package com.company.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryIssueDto {
    private String managerId;
    private String managerName;
    private double managerSalary;
    private double averageSubordinateSalary;
    private double expectedSalary;
    private double salaryDifference;
}
