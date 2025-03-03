package com.company.controller;

import com.company.dtos.ReportingLineDto;
import com.company.dtos.SalaryIssueDto;
import com.company.service.OrgAnalyzerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyzerController.class)
class AnalyzerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrgAnalyzerService analyzerService;

    private MockMultipartFile csvFile;

    @BeforeEach
    void setUp() {
        csvFile = new MockMultipartFile(
                "file",
                "employees.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "id,name,salary,manager_id\n1,CEO,200000,\n2,Manager,100000,1".getBytes()
        );
    }

    @Test
    void getUnderpaidManagers_shouldReturnOkResponse() throws Exception {

        SalaryIssueDto underpaidManager = new SalaryIssueDto();
        underpaidManager.setManagerId("3");
        underpaidManager.setManagerName("Manager3");
        underpaidManager.setManagerSalary(60000);
        underpaidManager.setAverageSubordinateSalary(61000);
        underpaidManager.setExpectedSalary(73200);
        underpaidManager.setSalaryDifference(-13200);

        List<SalaryIssueDto> underpaidManagers = Collections.singletonList(underpaidManager);


        when(analyzerService.getUnderpaidManagers(any())).thenReturn(underpaidManagers);


        mockMvc.perform(multipart("/api/v1/managers/underpaid")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].managerId").value("3"))
                .andExpect(jsonPath("$[0].managerName").value("Manager3"))
                .andExpect(jsonPath("$[0].salaryDifference").value(-13200));


        verify(analyzerService, times(1)).getUnderpaidManagers(any());
    }

    @Test
    void getUnderpaidManagers_whenNoUnderpaidManagers_shouldReturnEmptyList() throws Exception {

        when(analyzerService.getUnderpaidManagers(any())).thenReturn(Collections.emptyList());


        mockMvc.perform(multipart("/api/v1/managers/underpaid")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());


        verify(analyzerService, times(1)).getUnderpaidManagers(any());
    }

    @Test
    void getOverpaidManagers_shouldReturnOkResponse() throws Exception {
        SalaryIssueDto overpaidManager1 = new SalaryIssueDto();
        overpaidManager1.setManagerId("5");
        overpaidManager1.setManagerName("Manager5");
        overpaidManager1.setManagerSalary(95000);
        overpaidManager1.setAverageSubordinateSalary(57500);
        overpaidManager1.setExpectedSalary(86250);
        overpaidManager1.setSalaryDifference(8750);

        SalaryIssueDto overpaidManager2 = new SalaryIssueDto();
        overpaidManager2.setManagerId("7");
        overpaidManager2.setManagerName("Manager7");
        overpaidManager2.setManagerSalary(90000);
        overpaidManager2.setAverageSubordinateSalary(55000);
        overpaidManager2.setExpectedSalary(82500);
        overpaidManager2.setSalaryDifference(7500);

        List<SalaryIssueDto> overpaidManagers = Arrays.asList(overpaidManager1, overpaidManager2);
        when(analyzerService.getOverpaidManagers(any())).thenReturn(overpaidManagers);


        mockMvc.perform(multipart("/api/v1/managers/overpaid")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].managerId").value("5"))
                .andExpect(jsonPath("$[0].managerName").value("Manager5"))
                .andExpect(jsonPath("$[0].salaryDifference").value(8750))
                .andExpect(jsonPath("$[1].managerId").value("7"))
                .andExpect(jsonPath("$[1].managerName").value("Manager7"))
                .andExpect(jsonPath("$[1].salaryDifference").value(7500));


        verify(analyzerService, times(1)).getOverpaidManagers(any());
    }

    @Test
    void getOverpaidManagers_whenNoOverpaidManagers_shouldReturnEmptyList() throws Exception {

        when(analyzerService.getOverpaidManagers(any())).thenReturn(Collections.emptyList());


        mockMvc.perform(multipart("/api/v1/managers/overpaid")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());


        verify(analyzerService, times(1)).getOverpaidManagers(any());
    }

    @Test
    void getLongReportingLines_shouldReturnOkResponse() throws Exception {
        ReportingLineDto reportingLine = new ReportingLineDto();
        reportingLine.setEmployeeId("22");
        reportingLine.setEmployeeName("Employee22");
        reportingLine.setReportingLineDepth(6);
        reportingLine.setExcess(2);

        List<ReportingLineDto> reportingLines = Collections.singletonList(reportingLine);

        when(analyzerService.getLongReportingLines(any())).thenReturn(reportingLines);

        mockMvc.perform(multipart("/api/v1/employees/long-reporting-lines")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].employeeId").value("22"))
                .andExpect(jsonPath("$[0].employeeName").value("Employee22"))
                .andExpect(jsonPath("$[0].reportingLineDepth").value(6))
                .andExpect(jsonPath("$[0].excess").value(2));


        verify(analyzerService, times(1)).getLongReportingLines(any());
    }

    @Test
    void getLongReportingLines_whenNoLongReportingLines_shouldReturnEmptyList() throws Exception {
        when(analyzerService.getLongReportingLines(any())).thenReturn(Collections.emptyList());


        mockMvc.perform(multipart("/api/v1/employees/long-reporting-lines")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());


        verify(analyzerService, times(1)).getLongReportingLines(any());
    }

}