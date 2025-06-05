package io.github.toyota32k.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UtSortedListTest {
    private lateinit var numericList: UtSortedList<Int>
    private lateinit var stringList: UtSortedList<String>

    @Before
    fun setUp() {
        // 数値リスト（昇順）
        numericList = UtSortedList(UtSorter.ActionOnDuplicate.REJECT) { a, b -> a.compareTo(b) }

        // 文字列リスト（降順）
        stringList = UtSortedList(UtSorter.ActionOnDuplicate.ALLOW) { a, b -> b.compareTo(a) }
    }

    @Test
    fun testEmptyList() {
        assertEquals(0, numericList.size)
        assertTrue(numericList.isEmpty())
        assertFalse(numericList.contains(1))
    }

    @Test
    fun testAddSingleElement() {
        assertTrue(numericList.add(5))
        assertEquals(1, numericList.size)
        assertTrue(numericList.contains(5))
        assertEquals(5, numericList[0])
    }

    @Test
    fun testAddMultipleElementsSorted() {
        // 昇順にソートされることを確認
        numericList.add(5)
        numericList.add(3)
        numericList.add(7)
        numericList.add(1)

        assertEquals(4, numericList.size)
        assertEquals(1, numericList[0])
        assertEquals(3, numericList[1])
        assertEquals(5, numericList[2])
        assertEquals(7, numericList[3])
    }

    @Test
    fun testAddMultipleElementsReverseSorted() {
        // 降順にソートされることを確認
        stringList.add("apple")
        stringList.add("zebra")
        stringList.add("banana")

        assertEquals(3, stringList.size)
        assertEquals("zebra", stringList[0])
        assertEquals("banana", stringList[1])
        assertEquals("apple", stringList[2])
    }

    @Test
    fun testRejectDuplicates() {
        assertTrue(numericList.add(5))
        assertTrue(numericList.add(3))
        assertFalse(numericList.add(5)) // 重複は拒否
        assertEquals(2, numericList.size)
    }

    @Test
    fun testAllowDuplicates() {
        assertTrue(stringList.add("apple"))
        assertTrue(stringList.add("banana"))
        assertTrue(stringList.add("apple")) // 重複を許容
        assertEquals(3, stringList.size)
    }

    @Test
    fun testReplaceOnDuplicate() {
        // REPLACE モードの リスト
        val replaceList = UtSortedList(UtSorter.ActionOnDuplicate.REPLACE) { a: TestItem, b: TestItem ->
            a.id.compareTo(b.id)
        }

        val item1 = TestItem(1, "First")
        val item2 = TestItem(2, "Second")
        val item1Updated = TestItem(1, "Updated")

        assertTrue(replaceList.add(item1))
        assertTrue(replaceList.add(item2))
        assertTrue(replaceList.add(item1Updated)) // 同じIDは更新

        assertEquals(2, replaceList.size)
        assertEquals("Updated", replaceList[0].name)
    }

    @Test
    fun testAddAll() {
        val items = listOf(5, 3, 7, 1)
        assertTrue(numericList.addAll(items))

        assertEquals(4, numericList.size)
        assertEquals(listOf(1, 3, 5, 7), numericList)
    }

    @Test
    fun testAddAllAtIndex() {
        numericList.add(10)
        numericList.add(20)

        val items = listOf(5, 15, 25)
        assertTrue(numericList.addAll(1, items)) // インデックスは無視されるが、ソートされる

        assertEquals(5, numericList.size)
        assertEquals(listOf(5, 10, 15, 20, 25), numericList)
    }

    @Test
    fun testAddAtIndex() {
        numericList.add(10)
        numericList.add(30)

        numericList.add(1, 20) // インデックスは無視されるが、ソートされる

        assertEquals(3, numericList.size)
        assertEquals(listOf(10, 20, 30), numericList)
    }

    @Test
    fun testRemove() {
        numericList.add(10)
        numericList.add(20)
        numericList.add(30)

        assertTrue(numericList.remove(20))
        assertEquals(2, numericList.size)
        assertEquals(listOf(10, 30), numericList)
    }

    @Test
    fun testRemoveAt() {
        numericList.add(10)
        numericList.add(20)
        numericList.add(30)

        assertEquals(20, numericList.removeAt(1))
        assertEquals(2, numericList.size)
        assertEquals(listOf(10, 30), numericList)
    }

    @Test
    fun testSet() {
        numericList.add(10)
        numericList.add(20)
        numericList.add(30)

        // set操作はremoveAtとaddの組み合わせになる
        assertEquals(25, numericList.set(1, 25))

        assertEquals(3, numericList.size)
        assertEquals(listOf(10, 25, 30), numericList)
    }

    @Test
    fun testClear() {
        numericList.add(10)
        numericList.add(20)

        numericList.clear()
        assertTrue(numericList.isEmpty())
    }

    @Test
    fun testReplace() {
        val itemList = UtSortedList(UtSorter.ActionOnDuplicate.REJECT) { a: TestItem, b: TestItem ->
            a.id.compareTo(b.id)
        }

        val item1 = TestItem(1, "First")
        val item2 = TestItem(2, "Second")

        itemList.add(item1)
        itemList.add(item2)

        // 同じIDで名前が異なるアイテムで置換
        val item1Updated = TestItem(1, "Updated")
        assertTrue(itemList.replace(item1Updated))

        assertEquals(2, itemList.size)
        assertEquals("Updated", itemList[0].name)

        // 存在しないIDは置換できない
        val item3 = TestItem(3, "Third")
        assertFalse(itemList.replace(item3))
    }

    @Test
    fun testContainsAll() {
        numericList.add(10)
        numericList.add(20)
        numericList.add(30)

        assertTrue(numericList.containsAll(listOf(10, 30)))
        assertFalse(numericList.containsAll(listOf(10, 40)))
    }

    @Test
    fun testRetainAll() {
        numericList.add(10)
        numericList.add(20)
        numericList.add(30)
        numericList.add(40)

        assertTrue(numericList.retainAll(listOf(20, 40)))
        assertEquals(2, numericList.size)
        assertEquals(listOf(20, 40), numericList)
    }

    @Test
    fun testRemoveAll() {
        numericList.add(10)
        numericList.add(20)
        numericList.add(30)
        numericList.add(40)

        assertTrue(numericList.removeAll(listOf(20, 40)))
        assertEquals(2, numericList.size)
        assertEquals(listOf(10, 30), numericList)
    }

    @Test
    fun testIterator() {
        numericList.add(10)
        numericList.add(20)

        val iterator = numericList.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(10, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(20, iterator.next())
        assertFalse(iterator.hasNext())
    }

    // テスト用データクラス
    data class TestItem(val id: Int, val name: String)
}