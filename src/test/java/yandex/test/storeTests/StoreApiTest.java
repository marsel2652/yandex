package yandex.test.storeTests;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import yandex.test.configurations.BaseTest;
import yandex.test.models.InvalidOrder;
import yandex.test.models.Order;
import yandex.test.tools.ResponseParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StoreApiTest extends BaseTest {

    private final ResponseParser responseParser = new ResponseParser();

    private void compareStores(Order expectedOrder, Order actualOrder) {
        assertEquals(expectedOrder.getId(), actualOrder.getId(), "Pet ID does not match");
        assertEquals(expectedOrder.getPetId(), actualOrder.getPetId(), "Pet name does not match");
        assertEquals(expectedOrder.getStatus(), actualOrder.getStatus(), "Pet status does not match");
    }

    public void deleteOrder(int id) throws IOException {
        HttpDelete request = new HttpDelete(BASE_URL + "/store/order/" + id);
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }


    private Order createOrderPost() throws IOException {
        Order order = getCorrectOrder();

        HttpPost request = new HttpPost(BASE_URL + "/store/order");
        request.setEntity(new StringEntity(mapper.writeValueAsString(order)));
        request.setHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
        return order;
    }

    private Order getCorrectOrder() {
        Order order = new Order();
        order.setId(2);
        order.setQuantity(7);
        order.setShipDate("2024-11-27T17:57:18.307Z");
        order.setStatus("placed");
        order.setComplete(false);
        order.setPetId(1);
        return order;
    }

    @Test
    void testGetPetInventoryByStatusCorrect() throws IOException {
        HttpGet request = new HttpGet(BASE_URL + "/store/inventory");
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }

    @Test
    void testPostOrderCorrectOrder() throws IOException {
        Order order = getCorrectOrder();

        HttpPost request = new HttpPost(BASE_URL + "/store/order");
        request.setEntity(new StringEntity(mapper.writeValueAsString(order)));
        request.setHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }


    @Test
    void testPostOrderInvalidOrder() throws IOException {
        InvalidOrder invalidOrder = new InvalidOrder(getCorrectOrder());

        HttpPost request = new HttpPost(BASE_URL + "/store/order");
        request.setEntity(new StringEntity(mapper.writeValueAsString(invalidOrder)));
        request.setHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }


    @Test
    void testGetOrderExistedOrder() throws IOException {
        Order order = createOrderPost();

        HttpGet request = new HttpGet(BASE_URL + "/store/order/" + order.getId());
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
        Order gettingOrder = responseParser.getObjectFromResponse(response, Order.class);
        compareStores(gettingOrder, order);
    }

    @Test
    void testGetOrderNotExistedOrder() throws IOException {
        int orderId = 2;
        deleteOrder(orderId);
        HttpGet request = new HttpGet(BASE_URL + "/store/order/" + orderId);
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }


    @Test
    void testGetOrderInvalidIdBiggerThanTen() throws IOException {
        int orderId = 12;

        HttpGet request = new HttpGet(BASE_URL + "/store/order/" + orderId);
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }

    @Test
    void testDeleteOrderInvalidIdBiggerThanTen() throws IOException {
        Order order = createOrderPost();

        HttpDelete request = new HttpDelete(BASE_URL + "/store/order/" + order.getId());
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }

    @Test
    void testDeleteOrderNotFound() throws IOException {
        int invalidId = -1;

        HttpDelete request = new HttpDelete(BASE_URL + "/store/order/" + invalidId);
        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }


}
