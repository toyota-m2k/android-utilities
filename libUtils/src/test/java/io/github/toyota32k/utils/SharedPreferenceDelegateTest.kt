package io.github.toyota32k.utils

import android.content.SharedPreferences
import io.github.toyota32k.utils.android.SharedPreferenceDelegate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class SharedPreferenceDelegateTest {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        editor = Mockito.mock(SharedPreferences.Editor::class.java)
        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.putInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(editor)
        Mockito.`when`(editor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(editor)
        Mockito.`when`(editor.remove(Mockito.anyString())).thenReturn(editor)
    }

    @Test
    fun testPrefInt() {
        val key = "value"
        val delegate = SharedPreferenceDelegate(sharedPreferences)
        class TestClass {
            var value: Int by delegate.pref(42)
        }
        val test = TestClass()
        // get
        Mockito.`when`(sharedPreferences.getInt(key, 42)).thenReturn(42)
        Assert.assertEquals(42, test.value)
        // set
        test.value = 100
        verify(editor).putInt(key, 100)
        verify(editor, times(2)).apply()
    }

    @Test
    fun testPrefString() {
        val key = "value"
        val delegate = SharedPreferenceDelegate(sharedPreferences)
        class TestClass {
            var value: String by delegate.pref("default")
        }
        val test = TestClass()
        // get
        Mockito.`when`(sharedPreferences.getString(key, "default")).thenReturn("default")
        Assert.assertEquals("default", test.value)
        // set
        test.value = "changed"
        verify(editor).putString(key, "changed")
        verify(editor, times(2)).apply()
    }

    @Test
    fun testPrefNullable() {
        val key = "value"
        val delegate = SharedPreferenceDelegate(sharedPreferences)
        class TestClass {
            var value: String? by delegate.prefNullable()
        }
        val test = TestClass()
        // get null
        Mockito.`when`(sharedPreferences.getString(key, null)).thenReturn(null)
        Assert.assertNull(test.value)
        // set value
        test.value = "abc"
        verify(editor).putString(key, "abc")
        verify(editor, times(1)).apply()
        // set null
        test.value = null
        verify(editor).remove(key)
        verify(editor, times(2)).apply()
    }

    @Test
    fun testTypedPref() {
        data class User(val name: String, val age: Int)
        val key = "user"
        val delegate = SharedPreferenceDelegate(sharedPreferences)
        val gson = com.google.gson.Gson()
        class TestClass {
            var user: User by delegate.typedPref(User("default", 0), User::class.java)
        }
        val test = TestClass()
        // get default
        Mockito.`when`(sharedPreferences.getString(key, null)).thenReturn(null)
        Assert.assertEquals(User("default", 0), test.user)
        // set value
        val user = User("taro", 20)
        test.user = user
        verify(editor).putString(key, gson.toJson(user))
        // get value
        Mockito.`when`(sharedPreferences.getString(key, null)).thenReturn(gson.toJson(user))
        Assert.assertEquals(user, test.user)
    }

    @Test
    fun testTypedPrefNullable() {
        data class User(val name: String, val age: Int)
        val key = "user"
        val delegate = SharedPreferenceDelegate(sharedPreferences)
        val gson = com.google.gson.Gson()
        class TestClass {
            var user: User? by delegate.typedPrefNullable(User::class.java)
        }
        val test = TestClass()
        // get null
        Mockito.`when`(sharedPreferences.getString(key, null)).thenReturn(null)
        Assert.assertNull(test.user)
        // set value
        val user = User("jiro", 30)
        test.user = user
        verify(editor).putString(key, gson.toJson(user))
        // get value
        Mockito.`when`(sharedPreferences.getString(key, null)).thenReturn(gson.toJson(user))
        Assert.assertEquals(user, test.user)
        // set null
        test.user = null
        verify(editor).remove(key)
    }
}