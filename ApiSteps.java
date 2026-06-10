package stepdefinitions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class ApiSteps {

    private String baseUrl;
    private Response response;

    @Given("the API base URL {string}")
    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String path) {
        response = given()
                .baseUri(baseUrl)
                .when()
                .get(path)
                .then()
                .extract()
                .response();
    }

    @When("I send a POST request to {string} using file {string}")
    public void sendPostRequestFromFile(String path, String fileName) throws IOException {
        String body = readFile(fileName);

        response = given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(body)
                .when()
                .post(path)
                .then()
                .extract()
                .response();
    }

    private String readFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("File not found in classpath: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Then("the response status code should be {int}")
    public void verifyStatusCode(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode());
    }

    @Then("the response should contain {string}")
    public void verifyResponseBodyContains(String expectedValue) {
        assertTrue("Expected response to contain: " + expectedValue,
                response.getBody().asString().contains(expectedValue));
    }
}
