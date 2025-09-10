package com.champlain.enrollmentsservice.validation;

import com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RequestValidator {

    public static UnaryOperator<Mono<EnrollmentRequestModel>> validateBody() {
        return enrollmentRequestModelMono -> enrollmentRequestModelMono
                .filter(hasValidEnrollmentYear())
                .switchIfEmpty(ApplicationExceptions.invalidEnrollmentYear())
                .filter(hasSemester())
                .switchIfEmpty(ApplicationExceptions.missingSemester())
                .filter(hasStudentId())
                .switchIfEmpty(ApplicationExceptions.missingStudentId())
                .filter(hasCourseId())
                .switchIfEmpty(ApplicationExceptions.missingCourseId());

    }

    private static Predicate<EnrollmentRequestModel> hasValidEnrollmentYear() {
        return enrollmentRequestModel -> Objects.nonNull(enrollmentRequestModel.enrollmentYear());
    }

    private static Predicate<EnrollmentRequestModel> hasSemester() {
        return enrollmentRequestModel -> Objects.nonNull(enrollmentRequestModel.semester());
    }

    private static Predicate<EnrollmentRequestModel> hasStudentId() {
        return enrollmentRequestModel -> Objects.nonNull(enrollmentRequestModel.studentId());
    }

    private static Predicate<EnrollmentRequestModel> hasCourseId() {
        return enrollmentRequestModel -> Objects.nonNull(enrollmentRequestModel.courseId());
    }
}