package com.company.validator;

import com.company.exception.InvalidDataException;
import com.company.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validator for CSV data and organizational structure
 */
@Component
@Slf4j
public class CsvValidator {

    /**
     * Validates uploaded CSV file format
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDataException("CSV file is empty or not provided");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new InvalidDataException("Only CSV files are supported. Received: " +
                    (filename != null ? filename : "unknown file type"));
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new InvalidDataException("File size exceeds the maximum allowed limit of 10MB");
        }
    }

    /**
     * Validates employee data after parsing
     */
    public void validateEmployeeData(Map<String, Employee> employees) {
        if (employees == null) {
            throw new InvalidDataException("Failed to parse employee data");
        }

        if (employees.isEmpty()) {
            throw new InvalidDataException("No employee data found in the CSV file");
        }

        if (employees.size() > 1000) {
            throw new InvalidDataException("CSV file contains " + employees.size() +
                    " employees, which exceeds the maximum limit of 1000");
        }


        if (employees.size() < countUniqueIds(employees)) {
            throw new InvalidDataException("CSV file contains duplicate employee IDs");
        }


        for (Employee employee : employees.values()) {
            validateEmployee(employee);
        }


        validateManagerReferences(employees);
    }




    private int countUniqueIds(Map<String, Employee> employees) {
        Set<String> uniqueIds = new HashSet<>();
        for (Employee employee : employees.values()) {
            uniqueIds.add(employee.getId());
        }
        return uniqueIds.size();
    }

    private void validateEmployee(Employee employee) {
        if (employee.getId() == null || employee.getId().isEmpty()) {
            throw new InvalidDataException("Employee ID is missing");
        }

        if (employee.getName() == null || employee.getName().isEmpty()) {
            throw new InvalidDataException("Employee name is missing for ID: " + employee.getId());
        }

        if (employee.getSalary() < 0) {
            throw new InvalidDataException("Invalid negative salary value for employee " +
                    employee.getName() + " (ID: " + employee.getId() + ")");
        }
    }

    private void validateManagerReferences(Map<String, Employee> employees) {
        for (Employee employee : employees.values()) {
            String managerId = employee.getManagerId();
            if (managerId != null && !managerId.isEmpty() && !employees.containsKey(managerId)) {
                throw new InvalidDataException("Employee " + employee.getName() + " (ID: " + employee.getId() +
                        ") references a non-existent manager with ID: " + managerId);
            }

            if (managerId != null && managerId.equals(employee.getId())) {
                throw new InvalidDataException("Employee " + employee.getName() + " (ID: " + employee.getId() +
                        ") cannot be their own manager");
            }
        }
    }

}