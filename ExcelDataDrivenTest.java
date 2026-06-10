package runner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import model.ApiTestCase;
import utils.ExcelTestDataReader;

@RunWith(Parameterized.class)
public class ExcelDataDrivenTest {

    private final ApiTestCase testCase;

    public ExcelDataDrivenTest(ApiTestCase testCase) {
        this.testCase = testCase;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() throws IOException {
        List<ApiTestCase> cases = ExcelTestDataReader.read("testdata/api_test_cases.xlsx");
        return cases.stream()
                .filter(ApiTestCase::isExecute)
                .map(caseItem -> new Object[]{caseItem})
                .collect(Collectors.toList());
    }

    @Test
    public void executeExcelDrivenApiTest() throws IOException {
        Response response = sendRequest(testCase);
        assertAssertions(response, testCase.getTestToConduct());
    }

    private Response sendRequest(ApiTestCase testCase) throws IOException {
        String method = inferHttpMethod(testCase);
        String url = testCase.getUrl();
        String contentType = "json".equalsIgnoreCase(testCase.getSendType())
                ? "application/json"
                : "application/xml";

        if ("GET".equalsIgnoreCase(method)) {
            return given().contentType(contentType).when().get(url).then().extract().response();
        }

        String payload = readPayload(testCase);
        if (payload.isBlank()) {
            return given().contentType(contentType).when().post(url).then().extract().response();
        }

        return given().contentType(contentType).body(payload).when().post(url).then().extract().response();
    }

    private String inferHttpMethod(ApiTestCase testCase) {
        String description = testCase.getTestToConduct().toUpperCase();
        if (description.contains("POST")) {
            return "POST";
        }
        if (description.contains("PUT")) {
            return "PUT";
        }
        if (description.contains("DELETE")) {
            return "DELETE";
        }
        if (description.contains("PATCH")) {
            return "PATCH";
        }
        return testCase.getFileName() != null && !testCase.getFileName().isBlank() ? "POST" : "GET";
    }

    private String readPayload(ApiTestCase testCase) throws IOException {
        String fileName = testCase.getFileName();
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        String resourceName = testCase.getTestcaseFolder() == null || testCase.getTestcaseFolder().isBlank()
                ? fileName
                : testCase.getTestcaseFolder() + "/" + fileName;

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            inputStream = getClass().getClassLoader().getResourceAsStream("testdata/" + resourceName);
        }

        if (inputStream == null) {
            throw new IOException("Payload file not found: " + resourceName);
        }

        try (InputStream stream = inputStream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void assertAssertions(Response response, String testInstructions) {
        Pattern statusPattern = Pattern.compile("status\\s*[:=]\\s*(\\d{3})", Pattern.CASE_INSENSITIVE);
        Matcher statusMatcher = statusPattern.matcher(testInstructions);
        if (statusMatcher.find()) {
            int expected = Integer.parseInt(statusMatcher.group(1));
            assertTrue("Expected status code " + expected + " but was " + response.getStatusCode(),
                    response.getStatusCode() == expected);
        }

        Pattern containsPattern = Pattern.compile("contains\\s*[:=]\\s*\\\"?([^\\\"\\n,]+)\\\"?", Pattern.CASE_INSENSITIVE);
        Matcher containsMatcher = containsPattern.matcher(testInstructions);
        while (containsMatcher.find()) {
            String expectedText = containsMatcher.group(1).trim();
            assertTrue("Expected response body to contain '" + expectedText + "'",
                    response.getBody().asString().contains(expectedText));
        }
    }
}
