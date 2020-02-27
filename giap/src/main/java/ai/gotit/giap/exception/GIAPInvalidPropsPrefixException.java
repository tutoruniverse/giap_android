package ai.gotit.giap.exception;

public class GIAPInvalidPropsPrefixException extends GIAPRuntimeException {
    public GIAPInvalidPropsPrefixException() {
        super("Custom prop can not start with '_' character.");
    }
}
