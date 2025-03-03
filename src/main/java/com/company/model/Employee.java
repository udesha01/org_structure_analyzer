package com.company.model;

import java.util.ArrayList;
import java.util.List;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @CsvBindByName(column = "id")
    private String id;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "salary")
    private double salary;

    @CsvBindByName(column = "manager_id")
    private String managerId;

    private List<Employee> subordinates = new ArrayList<>();

    private int reportingLineDepth = -1; // -1 means not calculated yet

    public void addSubordinate(Employee subordinate) {
        subordinates.add(subordinate);
    }

    public double getAverageSubordinateSalary() {
        if (subordinates.isEmpty()) {
            return 0;
        }
        return subordinates.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0);
    }
}