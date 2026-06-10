package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import model.ApiTestCase;

public class ExcelTestDataReader {

    public static List<ApiTestCase> read(String fileName) throws IOException {
        List<ApiTestCase> cases = new ArrayList<>();

        try (InputStream inputStream = new FileInputStream("src/test/resources/" + fileName)) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Map<Integer, String> headers = readHeaders(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String executeValue = getCellValue(row, headers, "execute");
                boolean execute = "y".equalsIgnoreCase(executeValue)
                        || "yes".equalsIgnoreCase(executeValue)
                        || "true".equalsIgnoreCase(executeValue)
                        || "1".equals(executeValue);

                String sendType = getCellValue(row, headers, "sendtype");
                String fileNameValue = getCellValue(row, headers, "filename");
                String testcaseFolderValue = getCellValue(row, headers, "testcase folder");
                String urlValue = getCellValue(row, headers, "url");
                String testToConduct = getCellValue(row, headers, "testd to be conducted");

                cases.add(new ApiTestCase(execute, sendType, fileNameValue,
                        testcaseFolderValue, urlValue, testToConduct));
            }
        }

        return cases;
    }

    private static Map<Integer, String> readHeaders(Row headerRow) {
        Map<Integer, String> headers = new LinkedHashMap<>();
        for (Cell cell : headerRow) {
            String value = cellToString(cell).trim().toLowerCase();
            headers.put(cell.getColumnIndex(), value);
        }
        return headers;
    }

    private static String getCellValue(Row row, Map<Integer, String> headers, String expectedHeader) {
        for (Map.Entry<Integer, String> entry : headers.entrySet()) {
            if (entry.getValue().contains(expectedHeader)) {
                Cell cell = row.getCell(entry.getKey(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                return cellToString(cell);
            }
        }
        return "";
    }

    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
