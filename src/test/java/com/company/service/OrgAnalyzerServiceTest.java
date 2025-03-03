package com.company.service;

import com.company.dtos.ReportingLineDto;
import com.company.dtos.SalaryIssueDto;
import com.company.model.Employee;
import com.company.validator.CsvValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrgAnalyzerServiceTest {

    @Mock
    private CsvValidator csvValidator;

    @InjectMocks
    private OrgAnalyzerService orgAnalyzerService;

    private MultipartFile mockCsvFile;
    private String csvContent;

    @BeforeEach
    void setUp() {
        // Set up sample CSV content for testing
        csvContent = "id,name,salary,manager_id\n" +
                "1,CEO,200000,\n" +
                "2,CFO,150000,1\n" +
                "3,CTO,140000,1\n" +
                "4,Manager1,80000,2\n" +
                "5,Manager2,120000,2\n" +  // Overpaid manager (>50% of avg subordinate salary)
                "6,Manager3,75000,3\n" +    // Underpaid manager (<20% of avg subordinate salary)
                "7,Employee1,60000,4\n" +
                "8,Employee2,65000,4\n" +
                "9,Employee3,70000,5\n" +
                "10,Employee4,80000,5\n" +
                "11,Employee5,70000,6\n" +
                "12,Employee6,65000,6\n" +
                "13,Employee7,60000,7\n" +   // Deep reporting line (depth = 5)
                "14,Employee8,55000,13\n";   // Deeper reporting line (depth = 6)

        mockCsvFile = new MockMultipartFile(
                "employees.csv",
                "employees.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Mock the validator to avoid validation errors
        doNothing().when(csvValidator).validateFile(any(MultipartFile.class));
        doNothing().when(csvValidator).validateEmployeeData(any(Map.class));
    }

    @Test
    void getUnderpaidManagers_shouldReturnCorrectManagers() throws IOException {
        // When
        List<SalaryIssueDto> underpaidManagers = orgAnalyzerService.getUnderpaidManagers(mockCsvFile);

        // Then
        assertNotNull(underpaidManagers);
        assertEquals(3, underpaidManagers.size());

        // Verify validator was called
        verify(csvValidator).validateFile(mockCsvFile);
        verify(csvValidator).validateEmployeeData(any(Map.class));
    }

    @Test
    void getOverpaidManagers_shouldReturnCorrectManagers() throws IOException {
        // When
        List<SalaryIssueDto> overpaidManagers = orgAnalyzerService.getOverpaidManagers(mockCsvFile);

        // Then
        assertNotNull(overpaidManagers);
        assertEquals(2, overpaidManagers.size());


        // Verify validator was called
        verify(csvValidator).validateFile(mockCsvFile);
        verify(csvValidator).validateEmployeeData(any(Map.class));
    }

    @Test
    void getLongReportingLines_shouldReturnCorrectEmployees() throws IOException {
        // When
        List<ReportingLineDto> longReportingLines = orgAnalyzerService.getLongReportingLines(mockCsvFile);

        // Then
        assertNotNull(longReportingLines);
        assertEquals(1, longReportingLines.size());

        // Verify validator was called
        verify(csvValidator).validateFile(mockCsvFile);
        verify(csvValidator).validateEmployeeData(any(Map.class));
    }

    @Test
    void getUnderpaidManagers_withEmptyList_shouldReturnEmptyList() throws IOException {
        // Create CSV with no underpaid managers
        String csvWithNoUnderpaidManagers = "id,name,salary,manager_id\n" +
                "1,CEO,200000,\n" +
                "2,Manager,120000,1\n" +
                "3,Employee,80000,2\n";

        MultipartFile file = new MockMultipartFile(
                "employees.csv",
                "employees.csv",
                "text/csv",
                csvWithNoUnderpaidManagers.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<SalaryIssueDto> underpaidManagers = orgAnalyzerService.getUnderpaidManagers(file);

        // Then
        assertNotNull(underpaidManagers);
        assertTrue(underpaidManagers.isEmpty());
    }

    @Test
    void getOverpaidManagers_withEmptyList_shouldReturnEmptyList() throws IOException {
        // Create CSV with no overpaid managers
        String csvWithNoOverpaidManagers = "id,name,salary,manager_id\n" +
                "1,CEO,150000,\n" +
                "2,Manager,100000,1\n" +
                "3,Employee,80000,2\n";

        MultipartFile file = new MockMultipartFile(
                "employees.csv",
                "employees.csv",
                "text/csv",
                csvWithNoOverpaidManagers.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<SalaryIssueDto> overpaidManagers = orgAnalyzerService.getOverpaidManagers(file);

        // Then
        assertNotNull(overpaidManagers);
        assertTrue(overpaidManagers.isEmpty());
    }

    @Test
    void getLongReportingLines_withNoLongLines_shouldReturnEmptyList() throws IOException {
        // Create CSV with no long reporting lines
        String csvWithNoLongLines = "id,name,salary,manager_id\n" +
                "1,CEO,200000,\n" +
                "2,Manager,100000,1\n" +
                "3,Employee,80000,2\n";

        MultipartFile file = new MockMultipartFile(
                "employees.csv",
                "employees.csv",
                "text/csv",
                csvWithNoLongLines.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<ReportingLineDto> longLines = orgAnalyzerService.getLongReportingLines(file);

        // Then
        assertNotNull(longLines);
        assertTrue(longLines.isEmpty());
    }

    @Test
    void parseEmployeesFromFile_shouldHandleFileReadingError() throws IOException {
        // Create a mock file that will throw an exception when read
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getInputStream()).thenThrow(new IOException("Failed to read file"));

        // When
        List<SalaryIssueDto> result = orgAnalyzerService.getUnderpaidManagers(badFile);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify validator was called
        verify(csvValidator).validateFile(badFile);
    }


}