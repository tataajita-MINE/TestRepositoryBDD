package model;

public class ApiTestCase {
    private final boolean execute;
    private final String sendType;
    private final String fileName;
    private final String testcaseFolder;
    private final String url;
    private final String testToConduct;

    public ApiTestCase(boolean execute, String sendType, String fileName,
                       String testcaseFolder, String url, String testToConduct) {
        this.execute = execute;
        this.sendType = sendType;
        this.fileName = fileName;
        this.testcaseFolder = testcaseFolder;
        this.url = url;
        this.testToConduct = testToConduct;
    }

    public boolean isExecute() {
        return execute;
    }

    public String getSendType() {
        return sendType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTestcaseFolder() {
        return testcaseFolder;
    }

    public String getUrl() {
        return url;
    }

    public String getTestToConduct() {
        return testToConduct;
    }

    @Override
    public String toString() {
        return testToConduct + " -> " + url;
    }
}
