package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import com.champlain.courseservice.presentationlayer.CourseResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CourseService {
    Flux<CourseResponseModel> getCourses();
    Mono<CourseResponseModel> getCourseByCourseId(String courseId);
    Mono<CourseResponseModel> addCourse(Mono<CourseRequestModel> course);
    Mono<CourseResponseModel> updateCourse(Mono<CourseRequestModel> courseRequestModel, String courseId);
    Mono<CourseResponseModel> deleteCourse(String courseId);
}
