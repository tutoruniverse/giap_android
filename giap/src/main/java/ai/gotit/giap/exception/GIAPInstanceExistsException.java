package ai.gotit.giap.exception;

public class GIAPInstanceExistsException extends GIAPException {
    public GIAPInstanceExistsException() {
        super("Instance exists. Call getInstance() instead.");
    }
}
