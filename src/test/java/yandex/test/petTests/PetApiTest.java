package yandex.test.petTests;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Test;
import yandex.test.configurations.BaseTest;
import yandex.test.models.Category;
import yandex.test.models.InvalidPet;
import yandex.test.models.Pet;
import yandex.test.models.Tag;
import yandex.test.tools.ResponseParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PetApiTest extends BaseTest {
    private final ResponseParser parser = new ResponseParser();

    private boolean checkPetStatus(Pet[] pets, String status) {
        return Arrays.stream(pets).allMatch(pet -> Objects.equals(pet.getStatus(), status));
    }

    private Pet getCorrectPetExample() {
        Pet pet = new Pet();
        pet.setId(100);
        pet.setName("dog");
        pet.setStatus("available");
        pet.setCategory(new Category(3, "dogs"));
        pet.setPhotoUrls(List.of(new String[]{"example1.jpg", "example2.jpg"}));
        pet.setTags(Collections.singletonList(new Tag(1, "test Tag")));
        return pet;
    }


    private File createTempFile() throws IOException {
        File tempFile = File.createTempFile("testImage", ".jpg");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), new byte[0]);
        return tempFile;
    }

    private Pet getPet(long id) throws IOException {
        HttpGet request = new HttpGet(BASE_URL + "/pet/" + id);

        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());


        return parser.getObjectFromResponse(response, Pet.class);
    }

    private void comparePets(Pet expectedPet, Pet actualPet) {
        assertEquals(expectedPet.getId(), actualPet.getId(), "Pet ID does not match");
        assertEquals(expectedPet.getName(), actualPet.getName(), "Pet name does not match");
        assertEquals(expectedPet.getStatus(), actualPet.getStatus(), "Pet status does not match");
    }


    private Pet createValidPet() throws IOException {
        Pet pet = getCorrectPetExample();
        HttpPost request = new HttpPost(BASE_URL + "/pet");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(pet)));
        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
        return pet;
    }

    private void deletePetById(long id) throws IOException {
        HttpDelete deleteRequest = new HttpDelete(BASE_URL + "/pet/" + id);
        httpClient.execute(deleteRequest);
    }

    @Test
    void testCreateCorrectNewPetCode() throws IOException {
        Pet pet = getCorrectPetExample();
        HttpPost request = new HttpPost(BASE_URL + "/pet");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(pet)));

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getCode());
    }

    @Test
    void testCreateInvalidNewPetCode() throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/pet");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(""));

        HttpResponse response = httpClient.execute(request);
        assertEquals(405, response.getCode());
    }

    @Test
    void testUploadCorrectImage() throws IOException {
        Pet pet = getCorrectPetExample();

        String additionalMetadata = "test metadata";


        File tempFile = createTempFile();

        HttpPost request = new HttpPost(BASE_URL + "/pet/" + pet.getId() + "/uploadImage");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("additionalMetadata", additionalMetadata, ContentType.TEXT_PLAIN);
        builder.addBinaryBody("file", tempFile, ContentType.APPLICATION_OCTET_STREAM, tempFile.getName());

        request.setEntity(builder.build());

        HttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());
    }


    @Test
    void testUpdateInvalidIdPet() throws IOException {
        Pet pet = getCorrectPetExample();
        InvalidPet invalidPet = new InvalidPet(pet);
        invalidPet.setId("fdsfdsfsdfds");

        HttpDelete deleteRequest = new HttpDelete(BASE_URL + "/pet/" + -1);
        httpClient.execute(deleteRequest);

        HttpPut request = new HttpPut(BASE_URL + "/pet");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(invalidPet)));

        HttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }


    @Test
    void testUpdateEmptyBody() throws IOException {
        HttpPut request = new HttpPut(BASE_URL + "/pet");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(""));

        HttpResponse response = httpClient.execute(request);
        assertEquals(405, response.getCode());
    }


    @Test
    void testUpdateNonExistingPet() throws IOException {
        Pet pet = getCorrectPetExample();
        pet.setId(-1);
        deletePetById(-1);
        HttpPut request = new HttpPut(BASE_URL + "/pet");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(mapper.writeValueAsString(pet)));

        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }


    @Test
    void testUpdateCorrectPet() throws IOException {
        Pet pet = createValidPet();

        HttpPut requestUpdate = new HttpPut(BASE_URL + "/pet");
        requestUpdate.setHeader("Content-Type", "application/json");
        pet.setName("Vasya");
        requestUpdate.setEntity(new StringEntity(mapper.writeValueAsString(pet)));

        CloseableHttpResponse responseUpdate = httpClient.execute(requestUpdate);


        assertEquals(200, responseUpdate.getCode());
        Pet updatedPet = parser.getObjectFromResponse(responseUpdate, Pet.class);
        assertEquals("Vasya", updatedPet.getName());
        assertEquals(pet.getId(), updatedPet.getId());
        responseUpdate.close();
    }


    @Test
    void testFindPetsByStatus() throws IOException {

        String status = "available";
        HttpGet request = new HttpGet(BASE_URL + "/pet/findByStatus?status=" + status);

        CloseableHttpResponse response = httpClient.execute(request);
        Pet[] pets = mapper.readValue(response.getEntity().getContent(), Pet[].class);

        assertEquals(200, response.getCode());
        assertTrue(checkPetStatus(pets, status));
        response.close();
    }

    @Test
    void testFindPetsByInvalidStatus() throws IOException {
        String status = "invalid_status";
        HttpGet request = new HttpGet(BASE_URL + "/pet/findByStatus?status=" + status);

        HttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }


    @Test
    void testFindCorrectPetById() throws IOException {
        Pet pet = createValidPet();
        HttpGet request = new HttpGet(BASE_URL + "/pet/" + pet.getId());

        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());


        Pet petGet = parser.getObjectFromResponse(response, Pet.class);
        response.close();
        assertEquals(petGet.getId(), pet.getId());

    }


    @Test
    void testFindPetByInvalidId() throws IOException {
        String invalidId = "fdfdsfsdf"; // Неверный ID
        HttpGet request = new HttpGet(BASE_URL + "/pet/" + invalidId);

        HttpResponse response = httpClient.execute(request);
        assertEquals(400, response.getCode());
    }

    @Test
    void testFindNonExistingPetById() throws IOException {
        long id = 1;
        deletePetById(id);
        HttpGet request = new HttpGet(BASE_URL + "/pet/" + id);
        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }

    @Test
    void testUpdateCorrectFormData() throws IOException {
        Pet pet = createValidPet();
        String newName = "Vasya";
        String newStatus = "OK";
        HttpPost request = new HttpPost(BASE_URL + "/pet/" + pet.getId());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", newName));
        params.add(new BasicNameValuePair("status", newStatus));

        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            assertEquals(200, response.getCode());

            Pet newPet = getPet(pet.getId());
            assertTrue(Objects.equals(newPet.getName(), newName) &&
                    Objects.equals(newPet.getStatus(), newStatus));
        }
    }


    @Test
    void testUpdatePetIncorrectFormData() throws IOException {
        Pet pet = createValidPet();
        HttpPost request = new HttpPost(BASE_URL + "/pet/" + pet.getId());
        request.setEntity(new StringEntity(""));

        HttpResponse response = httpClient.execute(request);
        assertEquals(405, response.getCode());
    }


    @Test
    void testUpdatePetNotFoundPet() throws IOException {
        int randomId = 12;
        String newName = "Vasya";
        String newStatus = "OK";
        deletePetById(randomId);
        HttpPost request = new HttpPost(BASE_URL + "/pet/" + randomId);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", newName));
        params.add(new BasicNameValuePair("status", newStatus));

        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");


        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }


    @Test
    void testDeletePetCorrectId() throws IOException {
        Pet pet = createValidPet();
        deletePetById(pet.getId());
        HttpGet request = new HttpGet(BASE_URL + "/pet/" + pet.getId());

        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getCode());
    }


    @Test
    void testDeletePetInvalidId() throws IOException {
        String invalidId = "dsf";

        HttpDelete deleteRequest = new HttpDelete(BASE_URL + "/pet/" + invalidId);
        HttpResponse response = httpClient.execute(deleteRequest);

        assertEquals(400, response.getCode());
    }


    @Test
    void testMultipleMethods() throws IOException {
        Pet pet = createValidPet();
        HttpGet request = new HttpGet(BASE_URL + "/pet/" + pet.getId());

        CloseableHttpResponse response = httpClient.execute(request);
        assertEquals(200, response.getCode());


        Pet gettingPet = parser.getObjectFromResponse(response, Pet.class);
        comparePets(gettingPet, pet);

        HttpPut requestUpdate = new HttpPut(BASE_URL + "/pet");
        requestUpdate.setHeader("Content-Type", "application/json");
        pet.setName("Vasya");
        requestUpdate.setEntity(new StringEntity(mapper.writeValueAsString(gettingPet)));

        CloseableHttpResponse responseUpdate = httpClient.execute(requestUpdate);
        Pet updatedPet = parser.getObjectFromResponse(responseUpdate, Pet.class);
        comparePets(gettingPet, updatedPet);


        deletePetById(pet.getId());
        HttpGet requestGetDeleted = new HttpGet(BASE_URL + "/pet/" + pet.getId());

        HttpResponse responseGetDeleted = httpClient.execute(requestGetDeleted);
        assertEquals(404, responseGetDeleted.getCode());
    }

}
