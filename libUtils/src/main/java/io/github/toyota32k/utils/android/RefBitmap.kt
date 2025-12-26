package io.github.toyota32k.utils.android

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.core.graphics.scale
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.UtLib
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Closeable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Bitmap を参照カウンタで管理するクラス。
 * 作成時点では参照カウンタはゼロ。保持する場合は addRef()、そうでない場合は release()する。
 */
class RefBitmap(bmp:Bitmap) {
    private var bitmapEntity:Bitmap? = bmp
    private var refCount: Int = 0

    init {
        UtLib.logger.debug {
            "RefBitmap created ${bmp.hashCode()}"
        }
    }


    fun addRef(): RefBitmap {
        if (!hasBitmap) {
            UtLib.logger.error("RefBitmap doesn't have bitmap.")
            throw IllegalStateException("bitmap is already recycled")
        }
        refCount++
        return this
    }
    fun release() {
        refCount--
        if (refCount<=0) {
            UtLib.logger.debug { "RefBitmap released ${bitmap.hashCode()} refCount=$refCount" }
            if (bitmapEntity?.isRecycled==false) {
                bitmapEntity?.recycle()
            }
            bitmapEntity = null
        }
    }

    /**
     * addRef()してから block(bitmap) を実行し、終わったら release()する。
     * bitmapがリサイクル済みならIllegalStateExceptionをスロー
     *
     * bitmap または、bitmapOrNull フィールドを直接使うことを制限はしないが、
     * 不要になったら release() を確実に行うため、bitmap を使う場合は、addRef()/release() することを推奨。
     * use を使えば、これを自動化できる。
     */
    inline fun <reified T> use(block:(Bitmap)->T):T {
        val bmp = bitmapOrNull ?: throw IllegalStateException("bitmap is already recycled")
        addRef()
        return try {
            block(bmp)
        } catch (e:Throwable) {
            release()
            throw e
        }
    }

    /**
     * 例外を投げない use()
     * bitmapがリサイクル済みなら block を実行しないで def を返す。
     * block が例外を投げた場合も、def を返す。
     */
    inline fun <reified T> use(def:T, block:(Bitmap)->T):T {
        return try {
            use(block)
        } catch (e:Throwable) {
            def
        }
    }

    val hasBitmap get() = bitmapEntity?.isRecycled==false
    val bitmap: Bitmap get() = bitmapEntity ?: throw java.lang.IllegalStateException("bitmap is already recycled")
    val bitmapOrNull:Bitmap? get() = bitmapEntity?.takeIf { !it.isRecycled }
    val width:Int get() = bitmap.width
    val height:Int get() = bitmap.height
    fun scale(width:Int, height:Int, filter:Boolean=true):RefBitmap {
        return bitmap.scale(width, height, filter).toRef()
    }
    fun crop(sx:Int, sy:Int, width:Int, height:Int):RefBitmap {
        return createBitmap(this, sx, sy, width, height)
    }
    fun rotate(angle:Float):RefBitmap {
        if(angle==0f) return this
        val matrix = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true).toRef()
    }

    companion object {
        fun Bitmap.toRef():RefBitmap {
            return RefBitmap(this)
        }
        fun createBitmap(ref:RefBitmap, sx:Int, sy:Int, width:Int, height:Int): RefBitmap {
            return Bitmap.createBitmap(ref.bitmap, sx, sy, width, height).toRef()
        }
    }
}

/**
 * RefBitmap を保持するクラス。
 * このオブジェクトに RefBitmapを設定する（set()/constructor)ときに、自動的にaddRef()され、
 * reset()または、close()でrelease()される。
 * また、他のRefBitmap(null可）をセットすると、古いRefBitmapはreleaseされる。
 *
 * ReadWritePropertyを継承しており、ViewやViewModelのメンバ変数（フィールド）として利用されることを想定。
 *     var bmp: RefBitmap by RefBitmapHolder()
 */
class RefBitmapHolder(bx:RefBitmap?=null): Closeable, IDisposable, ReadWriteProperty<Any, RefBitmap?> {
    var refBitmap: RefBitmap? = bx?.apply { addRef() }
        private set

    fun set(br:RefBitmap?) {
        // old == br の場合に備え、先に addRef()する
        val old = refBitmap
        refBitmap = br?.apply { addRef() }
        old?.release()
    }
    fun reset() {
        refBitmap?.release()
        refBitmap = null
    }

    fun get():RefBitmap {
        val ref = refBitmap
        return if (ref?.hasBitmap==true) ref else throw IllegalStateException("bitmap is already recycled")
    }
    fun getOrNull():RefBitmap? {
        val ref = refBitmap
        return if (ref?.hasBitmap==true) ref else null
    }

    val hasBitmap:Boolean get() = refBitmap?.hasBitmap==true

    override fun close() {
        UtLib.logger.debug()
        reset()
    }

    override fun dispose() {
        UtLib.logger.debug()
        reset()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): RefBitmap? {
        return getOrNull()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: RefBitmap?) {
        set(value)
    }
}


/**
 * RefBitmap を lazy 的に保持することで、NonNull扱いとした RefBitmapHolder亜種
 * ReadWritePropertyとして使うことしか想定していない。
 */
class LazyRefBitmapHolder() : Closeable, IDisposable, ReadWriteProperty<Any, RefBitmap> {
    constructor(bx:RefBitmap):this() { refBitmap = bx.apply { addRef()} }
    lateinit var refBitmap: RefBitmap

    private val actRefBitmap: RefBitmap? get()
        = if (::refBitmap.isInitialized) {
            refBitmap
        } else {
            null
        }

    override fun close() {
        actRefBitmap?.release()
    }

    override fun dispose() {
        actRefBitmap?.release()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): RefBitmap {
        return refBitmap
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: RefBitmap) {
        val old = actRefBitmap
        refBitmap = value.apply { addRef() }
        old?.release()
    }
}

/**
 * RefBitmap を保持する MutableStateFlow
 * val refBitmapFlow = RefBitmapFlow()
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class RefBitmapFlow private constructor(private val flow: MutableStateFlow<RefBitmap?>) : MutableStateFlow<RefBitmap?> by flow, IDisposable {
    constructor(ref:RefBitmap?=null):this(MutableStateFlow(ref)) { holder.set(ref) }
    private val holder = RefBitmapHolder()

    override fun dispose() {
        flow.value = null
        holder.dispose()
    }

    override var value: RefBitmap?
        get() = holder.getOrNull()
        set(value) {
            holder.set(value)
            flow.value = value
        }
}
