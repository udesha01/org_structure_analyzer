package com.company.controller;

import com.company.dtos.ReportingLineDto;
import com.company.dtos.SalaryIssueDto;
import com.company.service.OrgAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalyzerController {

@Autowired
public OrgAnalyzerService analyzerService;

    /**
     * Find managers who earn less than they should (based on subordinates)
     */
    @PostMapping(value = "/managers/underpaid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SalaryIssueDto>> getUnderpaidManagers(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(analyzerService.getUnderpaidManagers(file));
    }

    /**
     * Find managers who earn more than they should (based on subordinates)
     */
    @PostMapping(value = "/managers/overpaid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SalaryIssueDto>> getOverpaidManagers(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(analyzerService.getOverpaidManagers(file));
    }

    /**
     * Find employees with reporting lines that are too long
     */
    @PostMapping(value = "/employees/long-reporting-lines", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ReportingLineDto>> getLongReportingLines(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(analyzerService.getLongReportingLines(file));
    }
}