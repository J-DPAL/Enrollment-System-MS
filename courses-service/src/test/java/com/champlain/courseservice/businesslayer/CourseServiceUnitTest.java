package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.mapper.EntityModelMapper;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import com.champlain.courseservice.presentationlayer.CourseResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceUnitTest {

    @InjectMocks
    private CourseServiceImpl courseService;
    @Mock
    private CourseRepository courseRepository;

    String courseId01 = UUID.randomUUID().toString();
    String courseId02 = UUID.randomUUID().toString();
    String courseId03 = UUID.randomUUID().toString();

    Course course1 = Course.builder()
            .courseId(courseId01)
            .courseNumber("cat-100")
            .courseName("Web-Services")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course2 = Course.builder()
            .courseId(courseId02)
            .courseNumber("cat-200")
            .courseName("Web Services 2")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course3 = Course.builder()
            .courseId(courseId03)
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
                    assertEquals(courseResponseModel.courseId(), course1.getCourseId());
                    assertEquals(courseResponseModel.courseNumber(), course1.getCourseNumber());
                    assertEquals(courseResponseModel.courseName(), course1.getCourseName());
                    assertEquals(courseResponseModel.numHours(), course1.getNumHours());
                    assertEquals(courseResponseModel.numCredits(), course1.getNumCredits());
                    assertEquals(courseResponseModel.department(), course1.getDepartment());
                    return true;
                })
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(courseResponseModel.courseId(), course2.getCourseId());
                    assertEquals(courseResponseModel.courseNumber(), course2.getCourseNumber());
                    assertEquals(courseResponseModel.courseName(), course2.getCourseName());
                    assertEquals(courseResponseModel.numHours(), course2.getNumHours());
                    assertEquals(courseResponseModel.numCredits(), course2.getNumCredits());
                    assertEquals(courseResponseModel.department(), course2.getDepartment());
                    return true;
                })
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(courseResponseModel.courseId(), course3.getCourseId());
                    assertEquals(courseResponseModel.courseNumber(), course3.getCourseNumber());
                    assertEquals(courseResponseModel.courseName(), course3.getCourseName());
                    assertEquals(courseResponseModel.numHours(), course3.getNumHours());
                    assertEquals(courseResponseModel.numCredits(), course3.getNumCredits());
                    assertEquals(courseResponseModel.department(), course3.getDepartment());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenGetCourseById_withExistingValidCourseId_thenReturnCourse() {
        when(courseRepository.findCourseByCourseId(courseId01))
            .thenReturn(Mono.just(course1));

        Mono<CourseResponseModel> result = courseService.getCourseByCourseId(courseId01);
        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(courseResponseModel.courseNumber(), course1.getCourseNumber());
                    assertEquals(courseResponseModel.courseId(), course1.getCourseId());
                    assertEquals(courseResponseModel.courseName(), course1.getCourseName());
                    assertEquals(courseResponseModel.numHours(), course1.getNumHours());
                    assertEquals(courseResponseModel.numCredits(), course1.getNumCredits());
                    assertEquals(courseResponseModel.department(), course1.getDepartment());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenAddCourse_thenReturnCourse() {
        CourseRequestModel request = new CourseRequestModel(
                course1.getCourseNumber(),
                course1.getCourseName(),
                course1.getNumHours(),
                course1.getNumCredits(),
                course1.getDepartment()
        );

        when(courseRepository.save(any(Course.class)))
                .thenReturn(Mono.just(course1));

        Mono<CourseResponseModel> result = courseService.addCourse(Mono.just(request));

        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(courseResponseModel.courseNumber(), course1.getCourseNumber());
                    assertEquals(courseResponseModel.courseId(), course1.getCourseId());
                    assertEquals(courseResponseModel.courseName(), course1.getCourseName());
                    assertEquals(courseResponseModel.numHours(), course1.getNumHours());
                    assertEquals(courseResponseModel.numCredits(), course1.getNumCredits());
                    assertEquals(courseResponseModel.department(), course1.getDepartment());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenUpdateCourse_thenReturnCourse() {

        CourseRequestModel request = new CourseRequestModel(
                "jav-999",
                "Java 01",
                45,
                3.0,
                "Computer Science"
        );

        when(courseRepository.findCourseByCourseId(courseId01))
                .thenReturn(Mono.just(course1));

        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> {
                    Course toSave = invocation.getArgument(0);
                    return Mono.just(toSave);
                });

        Mono<CourseResponseModel> result = courseService.updateCourse(Mono.just(request), courseId01);

        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals("jav-999", courseResponseModel.courseNumber());
                    assertEquals(course1.getCourseId(), courseResponseModel.courseId());
                    assertEquals("Java 01", courseResponseModel.courseName());
                    assertEquals(45, courseResponseModel.numHours());
                    assertEquals(3.0, courseResponseModel.numCredits());
                    assertEquals("Computer Science", courseResponseModel.department());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenDeleteCourse_thenReturnDeletedCourse() {
        when(courseRepository.findCourseByCourseId(courseId01))
                .thenReturn(Mono.just(course1));

        when(courseRepository.delete(course1))
                .thenReturn(Mono.empty());

        Mono<CourseResponseModel> result = courseService.deleteCourse(courseId01);

        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertEquals(course1.getCourseId(), courseResponseModel.courseId());
                    assertEquals(course1.getCourseNumber(), courseResponseModel.courseNumber());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenGetCourseById_withNonExistingCourseId_thenReturnEmpty() {
        String nonExistingCourseId = UUID.randomUUID().toString();

        when(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .thenReturn(Mono.empty());

        Mono<CourseResponseModel> result = courseService.getCourseByCourseId(nonExistingCourseId);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenUpdateCourse_withNonExistingCourseId_thenReturnEmpty() {
        String nonExistingCourseId = UUID.randomUUID().toString();
        CourseRequestModel request = new CourseRequestModel(
                "cat-999",
                "Updated Course",
                45,
                3.0,
                "Computer Science"
        );

        when(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .thenReturn(Mono.empty());

        Mono<CourseResponseModel> result = courseService.updateCourse(Mono.just(request), nonExistingCourseId);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenDeleteCourse_withNonExistingCourseId_thenReturnEmpty() {
        String nonExistingCourseId = UUID.randomUUID().toString();
        when(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .thenReturn(Mono.empty());

        Mono<CourseResponseModel> result = courseService.deleteCourse(nonExistingCourseId);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }


}