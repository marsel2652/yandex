package yandex.test.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {
    protected static final String BASE_URL = "https://petstore.swagger.io/v2";
    protected static CloseableHttpClient httpClient;
    protected static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        httpClient = HttpClients.createDefault();
        mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeSerializer localDateSerializer = new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern("yyyy-MM" +
                        "-dd'T'HH:mm" +
                        ":ss" +
                        ".SSSZ"));
        module.addSerializer(LocalDateTime.class, localDateSerializer);
        mapper.registerModule(module);


    }
}