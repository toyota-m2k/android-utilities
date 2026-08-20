package io.github.toyota32k.utils.android

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import io.github.toyota32k.utils.UtLib
import io.github.toyota32k.utils.UtLib.logger
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files

/**
 * File と Uri+Context によるファイルの扱いを抽象化するi/f
 */
interface IUtFile: Comparable<IUtFile> {
    val safeUri:Uri
    fun getLength():Long
    fun getFileName() : String?
    fun getContentType() : String?
    fun getLastModifiedTime():Long?
    fun exists(): Boolean
    fun <T> fileInputStream(fn:(FileInputStream)->T):T
    fun <T> fileOutputStream(fn:(FileOutputStream)->T):T
    fun delete()
    fun safeDelete()
    fun canWrite():Boolean
    fun copyFrom(src:IUtFile)
}

/**
 * 低レベルAPIを利用するための拡張i/f
 */
interface IUtFileEx: IUtFile {
    fun openParcelFileDescriptorToRead() :ParcelFileDescriptor
    fun openParcelFileDescriptorToWrite():ParcelFileDescriptor
}

/**
 * IUtFileExの共通実装
 */
abstract class UtFile:IUtFileEx {
    companion object {
        fun fromUri(uri:Uri, context: Context=UtLib.applicationContext):UtFile {
            return when (uri.scheme) {
                "content" -> UtContentFile(uri, context)
                "file" -> UtJavaFile(File(uri.path!!))
                else -> throw IllegalArgumentException("invalid uri")
            }
        }
        fun fromFile(file:File):UtFile {
            return UtJavaFile(file)
        }
    }

    protected abstract fun <T> withFileDescriptor(mode:String, fn:(FileDescriptor)->T):T
    private fun <T> fileDescriptorToRead(fn:(FileDescriptor)->T):T = withFileDescriptor("r", fn)
    private fun <T> fileDescriptorToWrite(fn:(FileDescriptor)->T):T = withFileDescriptor("rwt", fn)
    override fun <T> fileInputStream(fn:(FileInputStream)->T):T {
        return fileDescriptorToRead {
            FileInputStream(it).use(fn)
        }
    }
    override fun <T> fileOutputStream(fn:(FileOutputStream)->T):T {
        return fileDescriptorToWrite {
            FileOutputStream(it).use(fn)
        }
    }

    override fun safeDelete() {
        runCatching { delete() }
    }


    override fun copyFrom(src:IUtFile) {
        src.fileInputStream { input->
            this.fileOutputStream { output->
                input.channel.transferTo(0, input.channel.size(), output.channel)
            }
        }
    }
    override fun compareTo(other: IUtFile): Int {
        if (this == other) return 0
        return this.safeUri.compareTo(other.safeUri)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IUtFile) {
            return false
        }
        return compareTo(other) == 0
    }
    override fun hashCode(): Int {
        return safeUri.hashCode()
    }

    abstract fun openParcelFileDescriptor(mode:String):ParcelFileDescriptor

    override fun openParcelFileDescriptorToRead() = openParcelFileDescriptor("r")
    override fun openParcelFileDescriptorToWrite() = openParcelFileDescriptor("rw")

}

/**
 * java.io.File ベースの IUtFile実装
 */
class UtJavaFile(val path:File):UtFile() {
    override fun getLength(): Long {
        return try {
            path.length()
        } catch (e: Throwable) {
            logger.error(e)
            -1L
        }
    }

    override fun getLastModifiedTime(): Long? {
        return try {
            path.lastModified()
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }
    override fun <T> withFileDescriptor(mode:String, fn:(FileDescriptor)->T):T {
        return ParcelFileDescriptor.open(path, ParcelFileDescriptor.parseMode(mode)).use {
            fn(it.fileDescriptor)
        }
    }

    override fun delete() {
        path.delete()
    }

    override fun canWrite():Boolean {
        return path.canWrite()
    }
    override fun toString(): String {
        return path.toString()
    }

    override fun getFileName() : String? {
        return path.name
    }
    override fun getContentType() : String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.probeContentType(path.toPath())
        } else {
            val extension = path.extension.lowercase()
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }

    override val safeUri:Uri get() = path.toUri()

    override fun exists(): Boolean {
        return path.exists()
    }
    override fun openParcelFileDescriptor(mode:String):ParcelFileDescriptor {
        return ParcelFileDescriptor.open(path, ParcelFileDescriptor.parseMode(mode))
    }

}

/**
 * content:で始まるUriとContentResolver による IUtFile実装
 * file:も使えるように配慮はしているが、基本的に、file:は java.io.File として扱うことを推奨。
 */
class UtContentFile(val uri:Uri, val context: Context = UtLib.applicationContext):UtFile() {
    override fun getLength(): Long {
        val contentResolver = context.contentResolver ?: return -1L

        // ContentResolverから取得を試みる
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                return it.getLong(sizeIndex)
            }
        }
        // 取得できなければファイルを開いて取得する
        contentResolver.openFileDescriptor(uri, "r")?.use {
            return it.statSize
        }
        return -1L
    }

    override fun getLastModifiedTime(): Long? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    DocumentFile.fromSingleUri(context, uri)?.lastModified()
                }

                "file" -> {
                    uri.path?.run { File(this).lastModified() }
                }

                else -> null
            }
        } catch (_: Throwable) {
            return null
        }
    }

    override fun <T> withFileDescriptor(mode: String, fn: (FileDescriptor) -> T): T {
        return context.contentResolver.openAssetFileDescriptor(uri, mode)!!.use {
            fn(it.fileDescriptor)
        }
    }

    override fun delete() {
        DocumentFile.fromSingleUri(context, uri)?.delete()
            ?: throw IllegalStateException("invalid uri")
    }

    override fun canWrite(): Boolean {
        return DocumentFile.fromSingleUri(context, uri)?.canWrite() == true
    }

    override fun toString(): String {
        return uri.toString()
    }

    override fun getFileName(): String? {
        return when (uri.scheme) {
            "content" -> {
                val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                val cursor: Cursor? =
                    context.contentResolver?.query(uri, projection, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                    } else null
                }
            }

            "file" -> uri.path?.let { File(it).name }
            else -> null
        }
    }
    override fun getContentType() : String? {
        return context.contentResolver.getType(uri)
    }
    override val safeUri:Uri get() = uri

    override fun exists(): Boolean {
        return when(uri.scheme) {
                "content"-> {
                    val cursor: Cursor? = context.contentResolver?.query(uri, null, null, null, null)
                    cursor?.use {
                        it.count > 0
                    } ?: false
                }
                "file"-> uri.path?.let { File(it).exists() } ?: false
                else -> false
            }
        }

    override fun openParcelFileDescriptor(mode:String):ParcelFileDescriptor {
        return context.contentResolver.openFileDescriptor(uri, mode )!!
    }
}

fun File.toUtFile(): UtFile {
    return UtFile.fromFile(this)
}

fun Uri.toUtFile(context: Context = UtLib.applicationContext): UtFile {
    return UtFile.fromUri(this, context)
}
