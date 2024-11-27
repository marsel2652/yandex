package yandex.test.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public class ResponseParser {

    protected static ObjectMapper mapper = new ObjectMapper();

    public <T> T getObjectFromResponse(CloseableHttpResponse response, Class<T> clazz) throws IOException {
        return mapper.readValue(response.getEntity().getContent(), clazz);
    }
}
