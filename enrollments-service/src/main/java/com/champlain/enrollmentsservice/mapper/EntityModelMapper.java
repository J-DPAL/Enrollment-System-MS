package com.champlain.enrollmentsservice.mapper;

import com.champlain.enrollmentsservice.businesslayer.enrollments.RequestContext;
import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;

import java.util.UUID;

public class EntityModelMapper {

    public static Enrollment toEntity(RequestContext requestContext) {
        return Enrollment.builder()
                .enrollmentId(generateUUIDString())
                .enrollmentYear(requestContext.getEnrollmentRequestModel().enrollmentYear())
                .semester(requestContext.getEnrollmentRequestModel().semester())
                .studentId(requestContext.getStudentResponseModel().studentId())
                .studentFirstName(requestContext.getStudentResponseModel().firstName())
                .studentLastName(requestContext.getStudentResponseModel().lastName())
                .courseId(requestContext.getCourseResponseModel().courseId())
                .courseName(requestContext.getCourseResponseModel().courseName())
                .courseNumber(requestContext.getCourseResponseModel().courseNumber())
                .build();
    }


    private static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }

    public static EnrollmentResponseModel toModel(Enrollment enrollment) {
        return new EnrollmentResponseModel(
                enrollment.getEnrollmentId(),
                enrollment.getEnrollmentYear(),
                enrollment.getSemester(),
                enrollment.getStudentId(),
                enrollment.getStudentFirstName(),
                enrollment.getStudentLastName(),
                enrollment.getCourseId(),
                enrollment.getCourseNumber(),
                enrollment.getCourseName()
        );
    }

//    public static Enrollment toUpdatedEntity(RequestContext requestContext, Enrollment existing) {
//        return Enrollment.builder()
//                .id(existing.getId())
//                .enrollmentId(existing.getEnrollmentId())
//                .enrollmentYear(requestContext.getEnrollmentRequestModel().enrollmentYear())
//                .semester(requestContext.getEnrollmentRequestModel().semester())
//                .studentId(requestContext.getStudentResponseModel().studentId())
//                .studentFirstName(requestContext.getStudentResponseModel().firstName())
//                .studentLastName(requestContext.getStudentResponseModel().lastName())
//                .courseId(requestContext.getCourseResponseModel().courseId())
//                .courseName(requestContext.getCourseResponseModel().courseName())
//                .courseNumber(requestContext.getCourseResponseModel().courseNumber())
//                .build();
//    }

}