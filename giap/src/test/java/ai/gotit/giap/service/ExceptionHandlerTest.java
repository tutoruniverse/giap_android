package ai.gotit.giap.service;

import org.junit.Before;
import org.junit.Test;

import ai.gotit.giap.GIAP;
import ai.gotit.giap.exception.GIAPTestRuntimeException;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExceptionHandlerTest {
    private ExceptionHandler exceptionHandler;
    private GIAP giapInstance;

    @Before
    public void setUp() throws Exception {
        giapInstance = mock(GIAP.class);
        exceptionHandler = spy(ExceptionHandler.makeInstance(giapInstance));
    }

    @Test
    public void makeInstance() {
        ExceptionHandler newInstance = ExceptionHandler.makeInstance(giapInstance);
        assertNotSame(newInstance, exceptionHandler);
    }

    @Test
    public void uncaughtException() {
        doThrow(GIAPTestRuntimeException.class).when(exceptionHandler).exit();
        Thread thread = mock(Thread.class);
        Throwable exception = mock(Exception.class);
        boolean calledExit = false;
        try {
            exceptionHandler.uncaughtException(thread, exception);
        } catch (GIAPTestRuntimeException e) {
            calledExit = true;
        }
        assertTrue(calledExit);
        verify(giapInstance, times(1)).onUncaughtException();
    }
}