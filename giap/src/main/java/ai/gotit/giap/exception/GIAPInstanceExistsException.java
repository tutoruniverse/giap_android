package ai.gotit.giap.exception;

public class GIAPInstanceExistsException extends GIAPRuntimeException {
    public GIAPInstanceExistsException() {
        super("Instance exists. Call getInstance() instead.");
    }
}
