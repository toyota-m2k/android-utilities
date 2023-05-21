package io.github.toyota32k.utils

// Collection の unchecked cast ワーニングを鎮める拡張API
// 型が明らかな場合は @Suppress("UNCHECKED_CAST") でもよいだろうが、どこの馬の骨ともわからないものなら、ちゃんとチェックしてやったらどうや、
// と思って作ってみた。

@Suppress("UNCHECKED_CAST")
inline fun <reified K, reified V> Map<*,*>.asMapOfType(): Map<K,V>? =
        if(keys.all { it is K } && values.all { it is V }) this as Map<K,V> else null

@Suppress("UNCHECKED_CAST")
inline fun <reified T> List<*>.asListOfType(): List<T>? =
        if(all {it is T}) this as List<T> else null

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Array<*>.asArrayOfType(): Array<T>? =
        if(all {it is T}) this as Array<T> else null

fun <T> List<T>.reverse():Iterable<T> {
    return Iterable {
        iterator {
            for (i in count()-1 downTo 0) {
                yield(get(i)!!)
            }
        }
    }
}

fun <S,E> Collection<E>.reduce(accumulator:S, fn:(acc:S,element:E)->S): S {
    val acc = accumulator
    forEach {
        fn(acc,it)
    }
    return acc
}

