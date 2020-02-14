package ai.gotit.giap.exception;

public class GIAPNetworkException extends GIAPRuntimeException {
    public GIAPNetworkException(int code) {
        super("Request failed: " + code);
    }

    public GIAPNetworkException(int code, String responseBody) {
        super("Request failed: " + code + " - " + responseBody);
    }
}
