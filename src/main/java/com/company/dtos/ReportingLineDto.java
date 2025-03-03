package com.company.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportingLineDto {
    private String employeeId;
    private String employeeName;
    private int reportingLineDepth;
    private int excess;
}
