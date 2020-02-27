package ai.gotit.giap.support;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;

import ai.gotit.giap.GIAP;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GIAPActivityLifecycleCallbacksTest {
    private GIAPActivityLifecycleCallbacks callbacks;
    private GIAP giapInstance;
    private Activity context = mock(Activity.class);

    @Before
    public void setUp() throws Exception {
        giapInstance = mock(GIAP.class);
        callbacks = new GIAPActivityLifecycleCallbacks(giapInstance);
    }

    @Test
    public void onActivityPaused() {
        callbacks.onActivityPaused(context);
        assertTrue(callbacks.getBackground());
        verify(giapInstance, times(1)).onPause();
    }

    @Test
    public void onActivityResumed() {
        // If first resume (not from background) then do nothing
        assertFalse(callbacks.getBackground());
        callbacks.onActivityResumed(context);
        verify(giapInstance, times(0)).onResume();

        // If back from background, do callback
        callbacks.onActivityPaused(context);
        assertTrue(callbacks.getBackground());
        callbacks.onActivityResumed(context);
        assertFalse(callbacks.getBackground());
        verify(giapInstance, times(1)).onResume();
    }
}