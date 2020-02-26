package ai.gotit.giap.service;

import android.app.Activity;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StorageTest {
    private Storage storage;
    private ConfigManager configManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private final String key = "test_key";

    @Before
    public void setUp() throws Exception {
        Activity context = mock(Activity.class);
        configManager = new ConfigManager();
        configManager.setToken("token-123");
        configManager.setContext(context);

        pref = mock(SharedPreferences.class);
        editor = mock(SharedPreferences.Editor.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(pref);
        when(pref.edit()).thenReturn(editor);

        storage = spy(Storage.makeInstance(configManager));
    }

    @Test
    public void putString() {
        storage.put(key, "string");
        verify(editor, times(1)).putString(key, "string");
        verify(editor, times(1)).commit();
    }

    @Test
    public void putBoolean() {
        storage.put(key, true);
        verify(editor, times(1)).putBoolean(key, true);
        verify(editor, times(1)).commit();
    }

    @Test
    public void putInteger() {
        storage.put(key, 123);
        verify(editor, times(1)).putInt(key, 123);
        verify(editor, times(1)).commit();
    }

    @Test
    public void putFloat() {
        storage.put(key, 123.4f);
        verify(editor, times(1)).putFloat(key, 123.4f);
        verify(editor, times(1)).commit();
    }

    @Test
    public void getString() {
        when(pref.getString(anyString(), nullable(String.class))).thenReturn("string");

        when(pref.contains(anyString())).thenReturn(false);
        assertNull(storage.getString(key));

        when(pref.contains(anyString())).thenReturn(true);
        assertEquals("string", storage.getString(key));
    }

    @Test
    public void getBoolean() {
        when(pref.getBoolean(anyString(), anyBoolean())).thenReturn(true);

        when(pref.contains(anyString())).thenReturn(false);
        assertNull(storage.getBoolean(key));

        when(pref.contains(anyString())).thenReturn(true);
        assertEquals(true, storage.getBoolean(key));
    }

    @Test
    public void getInt() {
        when(pref.getInt(anyString(), anyInt())).thenReturn(123);

        when(pref.contains(anyString())).thenReturn(false);
        assertNull(storage.getInt(key));

        when(pref.contains(anyString())).thenReturn(true);
        assertEquals(123, (int) storage.getInt(key));
    }

    @Test
    public void getFloat() {
        when(pref.getFloat(anyString(), anyFloat())).thenReturn(123.4f);

        when(pref.contains(anyString())).thenReturn(false);
        assertNull(storage.getFloat(key));

        when(pref.contains(anyString())).thenReturn(true);
        assertTrue(123.4f == (float) storage.getFloat(key));
    }

    @Test
    public void remove() {
        storage.remove(key);
        verify(editor, times(1)).remove(key);
        verify(editor, times(1)).commit();
    }
}