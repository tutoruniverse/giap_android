package ai.gotit.giap.mock;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class MockHttpStack extends BaseHttpStack {
    private HttpResponse mResponseToReturn;
    private String mLastUrl;
    private Map<String, String> mLastHeaders;
    private byte[] mLastPostBody;
    public String getLastUrl() {
        return mLastUrl;
    }
    public Map<String, String> getLastHeaders() {
        return mLastHeaders;
    }
    public byte[] getLastPostBody() {
        return mLastPostBody;
    }
    public void setResponseToReturn(HttpResponse response) {
        mResponseToReturn = response;
    }
    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws AuthFailureError {
        mLastUrl = request.getUrl();
        mLastHeaders = new HashMap<String, String>();
        if (request.getHeaders() != null) {
            mLastHeaders.putAll(request.getHeaders());
        }
        if (additionalHeaders != null) {
            mLastHeaders.putAll(additionalHeaders);
        }
        try {
            mLastPostBody = request.getPostBody();
        } catch (AuthFailureError e) {
            mLastPostBody = null;
        }
        return mResponseToReturn;
    }
}