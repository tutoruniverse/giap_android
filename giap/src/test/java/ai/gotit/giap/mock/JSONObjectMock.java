package ai.gotit.giap.mock;

import org.json.JSONObject;

import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONObjectMock extends JSONObject implements Mock<JSONObject> {
    @Override
    public JSONObject getMock() {
        JSONObject mockJSONObject = mock(JSONObject.class);
        when(mockJSONObject.keys()).thenReturn(new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return null;
            }
        });

        return mockJSONObject;
    }
}
