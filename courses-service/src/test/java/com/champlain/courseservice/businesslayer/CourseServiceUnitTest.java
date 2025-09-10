package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.presentationlayer.CourseResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceUnitTest {

    @InjectMocks
    private CourseServiceImpl courseService;
    @Mock
    private CourseRepository courseRepository;

    String courseId = UUID.randomUUID().toString();
    Course course1 = Course.builder()
            .courseId(courseId)
            .courseNumber("cat-100")
            .courseName("Web-Services")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course2 = Course.builder()
            .courseId(courseId)
            .courseNumber("cat-200")
            .courseName("Web Services 2")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course3 = Course.builder()
            .courseId(courseId)
            .courseNumber("cat-300")
            .courseName("Web Services 3")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    @Test
    public void whenGetAllCourses_thenReturnThreeCourses() {

        // arrange
        when(courseRepository.findAll())
                .thenReturn(Flux.just(course1, course2, course3));

        // act
        Flux<CourseResponseModel> result = courseService.getCourses();
        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(courseResponseModel.courseNumber(), course1.getCourseNumber());
                    return true;
                })
                .expectNextMatches(courseResponseModel -> courseResponseModel.courseNumber().equals(course2.getCourseNumber()))
                .expectNextMatches(courseResponseModel -> courseResponseModel.courseNumber().equals(course3.getCourseNumber()))
                .verifyComplete();
    }


}