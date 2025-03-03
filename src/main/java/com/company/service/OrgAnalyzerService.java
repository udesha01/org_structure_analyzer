package com.company.service;


import com.company.dtos.ReportingLineDto;
import com.company.dtos.SalaryIssueDto;
import com.company.model.Employee;
import com.company.validator.CsvValidator;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrgAnalyzerService {

    private static final int MAX_ALLOWED_REPORTING_DEPTH = 4;
    private static final double MIN_SALARY_MULTIPLIER = 1.2; // 20% more
    private static final double MAX_SALARY_MULTIPLIER = 1.5; // 50% more

    @Autowired
    public CsvValidator csvValidator;

    /**
     * Get underpaid managers
     */
    public List<SalaryIssueDto> getUnderpaidManagers(MultipartFile file) {
        csvValidator.validateFile(file);
        Map<String, Employee> employees = parseEmployeesFromFile(file);
        csvValidator.validateEmployeeData(employees);
        buildHierarchy(employees);
        List<SalaryIssueDto> results= identifySalaryIssues(employees);
        return results.stream()
                .filter(issue -> issue.getSalaryDifference() < 0)
                .collect(Collectors.toList());
    }

    /**
     * Get overpaid managers
     */
    public List<SalaryIssueDto> getOverpaidManagers(MultipartFile file) {
        csvValidator.validateFile(file);
        Map<String, Employee> employees = parseEmployeesFromFile(file);
        csvValidator.validateEmployeeData(employees);
        buildHierarchy(employees);
        List<SalaryIssueDto> results= identifySalaryIssues(employees);
        return results.stream()
                .filter(issue -> issue.getSalaryDifference() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Get employees with long reporting lines
     */
    public List<ReportingLineDto> getLongReportingLines(MultipartFile file) {
        csvValidator.validateFile(file);
        Map<String, Employee> employees = parseEmployeesFromFile(file);
        csvValidator.validateEmployeeData(employees);
        buildHierarchy(employees);
        Employee ceo = findCEO(employees);

        if (ceo != null) {
            calculateReportingLineDepths(ceo, 0);
        }

        return identifyReportingLineIssues(employees);
    }



    private Map<String, Employee> parseEmployeesFromFile(MultipartFile file) {
        Map<String, Employee> employees = new HashMap<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<Employee> employeeList = new CsvToBeanBuilder<Employee>(reader)
                    .withType(Employee.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            for (Employee employee : employeeList) {
                employees.put(employee.getId(), employee);
            }
        } catch (Exception e) {
            log.error("Error parsing CSV file", e);
        }

        return employees;
    }

    private void buildHierarchy(Map<String, Employee> employees) {
        for (Employee employee : employees.values()) {
            String managerId = employee.getManagerId();
            if (managerId != null && !managerId.isEmpty()) {
                Employee manager = employees.get(managerId);
                if (manager != null) {
                    manager.addSubordinate(employee);
                }
            }
        }
    }

    private Employee findCEO(Map<String, Employee> employees) {
        for (Employee employee : employees.values()) {
            if (employee.getManagerId() == null || employee.getManagerId().isEmpty()) {
                return employee;
            }
        }
        return null;
    }

    private void calculateReportingLineDepths(Employee employee, int depth) {
        employee.setReportingLineDepth(depth);
        for (Employee subordinate : employee.getSubordinates()) {
            calculateReportingLineDepths(subordinate, depth + 1);
        }
    }

    private List<SalaryIssueDto> identifySalaryIssues(Map<String, Employee> employees) {
        List<SalaryIssueDto> issues = new ArrayList<>();

        for (Employee employee : employees.values()) {
            if (!employee.getSubordinates().isEmpty()) {
                double avgSubordinateSalary = employee.getAverageSubordinateSalary();
                double minRequiredSalary = avgSubordinateSalary * MIN_SALARY_MULTIPLIER;
                double maxAllowedSalary = avgSubordinateSalary * MAX_SALARY_MULTIPLIER;

                if (employee.getSalary() < minRequiredSalary) {
                    issues.add(SalaryIssueDto.builder()
                            .managerId(employee.getId())
                            .managerName(employee.getName())
                            .managerSalary(employee.getSalary())
                            .averageSubordinateSalary(avgSubordinateSalary)
                            .expectedSalary(minRequiredSalary)
                            .salaryDifference(employee.getSalary() - minRequiredSalary)
                            .build());
                } else if (employee.getSalary() > maxAllowedSalary) {
                    issues.add(SalaryIssueDto.builder()
                            .managerId(employee.getId())
                            .managerName(employee.getName())
                            .managerSalary(employee.getSalary())
                            .averageSubordinateSalary(avgSubordinateSalary)
                            .expectedSalary(maxAllowedSalary)
                            .salaryDifference(employee.getSalary() - maxAllowedSalary)
                            .build());
                }
            }
        }

        return issues;
    }

    private List<ReportingLineDto> identifyReportingLineIssues(Map<String, Employee> employees) {
        List<ReportingLineDto> issues = new ArrayList<>();

        for (Employee employee : employees.values()) {
            int depth = employee.getReportingLineDepth();
            if (depth > MAX_ALLOWED_REPORTING_DEPTH) {
                issues.add(ReportingLineDto.builder()
                        .employeeId(employee.getId())
                        .employeeName(employee.getName())
                        .reportingLineDepth(depth)
                        .excess(depth - MAX_ALLOWED_REPORTING_DEPTH)
                        .build());
            }
        }

        return issues;
    }

}