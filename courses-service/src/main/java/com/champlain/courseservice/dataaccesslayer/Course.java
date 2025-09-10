package com.champlain.courseservice.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    private Integer id;
    private String courseId;
    private String courseNumber;
    private String courseName;
    private Integer numHours;
    private Double numCredits;
    private String department;

}

