package yandex.test.userTests;

import java.io.IOException;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import yandex.test.configurations.BaseTest;
import yandex.test.models.User;
import yandex.test.tools.ResponseParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserApiTests extends BaseTest {
    private final ResponseParser responseParser = new ResponseParser();

    private User getCorrectUser() {
        User user = new User();
        user.setId(2);
        user.setUsername("Petya");
        user.setUserStatus(200);
        user.setEmail("test");
        user.setPhone("123");
        user.setFirstName("John");
        user.setLastName("Brown");
        user.setPassword("password");
        return user;
    }

    private User createUser() throws IOException {
        User user = getCorrectUser();

        HttpPost request = new HttpPost(BASE_URL + "/user");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(user)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
        return user;
    }

    private void deleteUser(String username) throws IOException {
        HttpDelete deleteRequest = new HttpDelete(BASE_URL + "/user/" + username);
        httpClient.execute(deleteRequest);
    }

    @Test
    void testPostCreateUsersWithListCorrectUsers() throws IOException {
        User user1 = getCorrectUser();
        User user2 = getCorrectUser();
        user2.setId(3);
        user2.setUsername("Vasya");
        List<User> users = List.of(user1, user2);

        HttpPost request = new HttpPost(BASE_URL + "/user" + "/createWithList");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(users)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }

    @Test
    void testPostCreateUsersWithListIncorrect() throws IOException {

        List<Integer> users = List.of(1, 2);

        HttpPost request = new HttpPost(BASE_URL + "/user" + "/createWithList");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(users)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }


    @Test
    void testPostCreateUsersWithArrayCorrectUsers() throws IOException {
        User user1 = getCorrectUser();
        User user2 = getCorrectUser();
        user2.setId(3);
        user2.setUsername("Vasya");
        List<User> users = List.of(user1, user2);

        HttpPost request = new HttpPost(BASE_URL + "/user" + "/createWithArray");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(users)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }

    @Test
    void testPostCreateUsersWithArrayIncorrect() throws IOException {

        List<Integer> users = List.of(1, 2);

        HttpPost request = new HttpPost(BASE_URL + "/user" + "/createWithArray");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(users)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }


    @Test
    void testPostCreateUserCorrectUser() throws IOException {
        User user = getCorrectUser();

        HttpPost request = new HttpPost(BASE_URL + "/user");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(user)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }

    @Test
    void testPostCreateUserIncorrectData() throws IOException {

        HttpPost request = new HttpPost(BASE_URL + "/user");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(""));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }


    @Test
    void testGetUserExistedUser() throws IOException {
        User user = createUser();

        HttpGet request = new HttpGet(BASE_URL + "/user/" + user.getUsername());
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }

    @Test
    void testGetUserNotExistedUser() throws IOException {
        String userName = "user";
        deleteUser(userName);

        HttpGet request = new HttpGet(BASE_URL + "/user/" + userName);
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }

    @Test
    void testGetUserInvalidUserName() throws IOException {

        HttpGet request = new HttpGet(BASE_URL + "/user/");
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }


    @Test
    void testUserLogout() throws IOException {

        HttpGet request = new HttpGet(BASE_URL + "/user/logout");
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }

    @Test
    void testUserLoginCorrectUserPassword() throws IOException {
        User user = createUser();

        HttpGet request =
                new HttpGet(
                        BASE_URL + "/user/login?username=" + user.getUsername() + "&password=" + user.getPassword());

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }

    @Test
    void testUserLoginInvalidUserPassword() throws IOException {
        User user = createUser();
        user.setPassword("fdfsdfsdf");
        HttpGet request =
                new HttpGet(
                        BASE_URL + "/user/login?username=" + user.getUsername() + "&password=" + user.getPassword());

        HttpResponse response = httpClient.execute(request);

        assertEquals(400, response.getCode());
    }

    @Test
    void testUpdateCorrectUser() throws IOException {
        User user = createUser();
        user.setPassword("fdfsdfsdf");
        HttpPut request = new HttpPut(BASE_URL + "/user/" + user.getUsername());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(user)));

        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());

        HttpGet requestGet = new HttpGet(BASE_URL + "/user/" + user.getUsername());

        CloseableHttpResponse responseGet = httpClient.execute(requestGet);
        User updatedUser = responseParser.getObjectFromResponse(responseGet, User.class);

        assertEquals(user.getPassword(), updatedUser.getPassword());
    }

    @Test
    void testUpdateInvalidSuppliedUser() throws IOException {
        String randomUserName = "user1";
        HttpPut request = new HttpPut(BASE_URL + "/user/" + randomUserName);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(""));

        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }

    @Test
    void testUpdateNotExistedUser() throws IOException {
        String randomUserName = "user1";
        deleteUser(randomUserName);
        User user = getCorrectUser();
        HttpPut request = new HttpPut(BASE_URL + "/user/" + randomUserName);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(user)));

        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }

    @Test
    void testDeleteNotExistedUser() throws IOException {
        String randomUserName = "sdfjdsfj90us09dfu09ds";

        HttpDelete request = new HttpDelete(BASE_URL + "/user/" + randomUserName);
        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }

    @Test
    void testDeleteExistedUser() throws IOException {
        User user = createUser();

        HttpDelete request = new HttpDelete(BASE_URL + "/user/" + user.getUsername());
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }

    @Test
    void testComplexMethods() throws IOException {
        User user = createUser();


        HttpGet request = new HttpGet(BASE_URL + "/user/" + user.getUsername());
        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode(), "Get user failed");

        User addedUser = responseParser.getObjectFromResponse(response, User.class);
        assertEquals(user.getUsername(), addedUser.getUsername(), "Username does not match");

        addedUser.setPassword("fdfsdfsdf");
        HttpPut requestUpdate = new HttpPut(BASE_URL + "/user/" + user.getUsername());
        requestUpdate.setHeader("Content-Type", "application/json");
        requestUpdate.setEntity(new StringEntity(mapper.writeValueAsString(addedUser)));

        HttpResponse responseUpdate = httpClient.execute(requestUpdate);
        assertEquals(200, responseUpdate.getCode(), "Update user failed");


        HttpGet requestGet = new HttpGet(BASE_URL + "/user/" + user.getUsername());
        CloseableHttpResponse responseGet = httpClient.execute(requestGet);
        assertEquals(200, responseGet.getCode(), "Get updated user failed");

        User updatedUser = responseParser.getObjectFromResponse(responseGet, User.class);
        assertEquals(addedUser.getPassword(), updatedUser.getPassword(), "Password does not match");


        HttpDelete requestDelete = new HttpDelete(BASE_URL + "/user/" + user.getUsername());
        CloseableHttpResponse responseDelete = httpClient.execute(requestDelete);
        assertEquals(200, responseDelete.getCode(), "Delete user failed");
    }


}
