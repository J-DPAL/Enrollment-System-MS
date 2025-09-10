package com.champlain.courseservice.validation;

import com.champlain.courseservice.exceptionhandling.ApplicationExceptions;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RequestValidator {

    public static UnaryOperator<Mono<CourseRequestModel>> validateBody() {
        return courseRequestModelMono -> courseRequestModelMono
                .filter(hasCourseNumber())
                .switchIfEmpty(ApplicationExceptions.missingCourseNumber())
                .filter(hasCourseName())
                .switchIfEmpty(ApplicationExceptions.missingCourseName())
                .filter(hasValidCredits())
                .switchIfEmpty(ApplicationExceptions.invalidCourseCredits())
                .filter(hasValidHours())
                .switchIfEmpty(ApplicationExceptions.invalidCourseHours());

    }

    private static Predicate<CourseRequestModel> hasCourseNumber() {
        return courseRequestModel -> Objects.nonNull(courseRequestModel.courseNumber());
    }

    private static Predicate<CourseRequestModel> hasCourseName() {
        return courseRequestModel -> Objects.nonNull(courseRequestModel.courseName());
    }

    // A course needs to have credits & hours above 0
    private static Predicate<CourseRequestModel> hasValidCredits() {
        return courseRequestModel -> Objects.nonNull(courseRequestModel.numCredits())
                && (courseRequestModel.numCredits() > 0);
    }

    private static Predicate<CourseRequestModel> hasValidHours() {
        return courseRequestModel -> Objects.nonNull(courseRequestModel.numHours())
                && (courseRequestModel.numHours() > 0);
    }
}