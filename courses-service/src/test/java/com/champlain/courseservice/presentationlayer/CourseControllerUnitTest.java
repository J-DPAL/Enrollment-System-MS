package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.exceptionhandling.exceptions.CourseNotFoundException;
import com.champlain.courseservice.exceptionhandling.exceptions.InvalidCourseIdException;
import com.champlain.courseservice.exceptionhandling.exceptions.InvalidInputException;
import com.champlain.courseservice.validation.RequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseControllerUnitTest {

    @InjectMocks
    private CourseController courseController;

    @Mock
    private CourseService courseService;

    private final String EXISTING_COURSE_ID = "bc77167f-8082-4405-a182-7bbf7fb343a2";
    private final String NON_EXISTING_COURSE_ID = "bc77167f-8082-4405-a182-7bbf7fb343b2";
    private final String INVALID_COURSE_ID = "bc77167f-8082-4405-a182-7bbf7fb34";

    @Test
    public void whenGetCourseByCourseId_withNonExistingCourseId_thenThrowCourseNotFoundException() {
            // Arrange
            when(courseService.getCourseByCourseId(NON_EXISTING_COURSE_ID))
                    .thenReturn(Mono.empty());

            // Act
            Mono<ResponseEntity<CourseResponseModel>> result = courseController.getCourseByCourseId(NON_EXISTING_COURSE_ID);

            // Assert
            StepVerifier.create(result)
                    .expectErrorMatches(e -> e instanceof CourseNotFoundException && e.getMessage().equals("Course with id=" + NON_EXISTING_COURSE_ID + " is not found"))
                    .verify();
    }

    @Test
    public void whenAddCourse_withInvalidHours_thenThrowInvalidInputException() {
        CourseRequestModel courseRequestModel = new CourseRequestModel(
                "cat-423",
                "Web Services Testing",
                0,
                3.0,
                "Computer Science"
        );

        Mono<ResponseEntity<CourseResponseModel>> result = courseController.addCourse(Mono.just(courseRequestModel));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException && e.getMessage().equals("Course hours must be greater than 0"))
                .verify();
    }

    @Test
    public void whenUpdateCourse_withInvalidHours_thenThrowInvalidInputException() {
        CourseRequestModel courseRequestModel = new CourseRequestModel(
                "cat-423",
                "Web Services Testing",
                0,
                3.0,
                "Computer Science"
        );

        Mono<ResponseEntity<CourseResponseModel>> result = courseController.updateCourse(EXISTING_COURSE_ID, Mono.just(courseRequestModel));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException
                        && e.getMessage().equals("Course hours must be greater than 0"))
                .verify();
    }

    @Test
    public void whenGetCourseByCourseId_withInvalidCourseId_thenThrowCourseInvalidInputException() {
        // Act
        Mono<ResponseEntity<CourseResponseModel>> result = courseController.getCourseByCourseId(INVALID_COURSE_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidCourseIdException && e.getMessage().equals("Course id=" + INVALID_COURSE_ID + " is invalid"))
                .verify();
    }

    @Test
    public void whenGetAllCourses_thenGetAllCourses() {
        CourseResponseModel CourseResponse1 = new CourseResponseModel(
                EXISTING_COURSE_ID,
                "jav-01",
                "Java 01",
                40,
                3.0,
                "Computer Science"
        );

        CourseResponseModel CourseResponse2 = new CourseResponseModel(
                "bc77167f-8082-4405-a182-7bbf7fb39999",
                "jav-02",
                "Java 02",
                45,
                3.0,
                "Computer Science"
        );

        when(courseService.getCourses())
                .thenReturn(Flux.just(CourseResponse1, CourseResponse2));

        Flux<CourseResponseModel> result = courseController.getCourses();

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertNotNull(response);
                    assertEquals("jav-01", response.courseNumber());
                    assertEquals(EXISTING_COURSE_ID, response.courseId());
                    assertEquals("Java 01", response.courseName());
                    assertEquals(40, response.numHours());
                    assertEquals(3.0, response.numCredits());
                    assertEquals("Computer Science", response.department());
                    return true;
                })
                .expectNextMatches(response -> {
                    assertNotNull(response);
                    assertEquals("jav-02", response.courseNumber());
                    assertEquals("bc77167f-8082-4405-a182-7bbf7fb39999", response.courseId());
                    assertEquals("Java 02", response.courseName());
                    assertEquals(45, response.numHours());
                    assertEquals(3.0, response.numCredits());
                    assertEquals("Computer Science", response.department());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenGetCourseByCourseId_withExistingValidCourseId_thenReturnCourse() {
        // Arrange
        CourseResponseModel CourseResponse = new CourseResponseModel(
                EXISTING_COURSE_ID,
                "jav-01",
                "Java 01",
                40,
                3.0,
                "Computer Science"
        );

        when(courseService.getCourseByCourseId(EXISTING_COURSE_ID))
                .thenReturn(Mono.just(CourseResponse));

        // Act
        Mono<ResponseEntity<CourseResponseModel>> result = courseController.getCourseByCourseId(EXISTING_COURSE_ID);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    CourseResponseModel response = responseEntity.getBody();
                    assertNotNull(response);
                    assertEquals("jav-01", response.courseNumber());
                    assertEquals(EXISTING_COURSE_ID, response.courseId());
                    assertEquals("Java 01", response.courseName());
                    assertEquals(40, response.numHours());
                    assertEquals(3.0, response.numCredits());
                    assertEquals("Computer Science", response.department());
                    assertEquals(200, responseEntity.getStatusCode().value());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenAddCourse_withValidData_thenAddCourse() {
        CourseRequestModel request = new CourseRequestModel(
                "jav-999",
                "Java 01",
                45,
                3.0,
                "Computer Science"
        );

        CourseResponseModel responseModel = new CourseResponseModel(
                EXISTING_COURSE_ID,
                "jav-999",
                "Java 01",
                45,
                3.0,
                "Computer Science"
        );

        when(courseService.addCourse(any(Mono.class)))
                .thenReturn(Mono.just(responseModel));


        Mono<ResponseEntity<CourseResponseModel>> result = courseController.addCourse(Mono.just(request));

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    CourseResponseModel response = responseEntity.getBody();
                    assertNotNull(response);
                    assertEquals("jav-999", response.courseNumber());
                    assertEquals(EXISTING_COURSE_ID, response.courseId());
                    assertEquals("Java 01", response.courseName());
                    assertEquals(45, response.numHours());
                    assertEquals(3.0, response.numCredits());
                    assertEquals("Computer Science", response.department());
                    assertEquals(201, responseEntity.getStatusCode().value());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenUpdateCourse_withValidIdAndValidData_thenReturnUpdatedCourse() {
        CourseRequestModel request = new CourseRequestModel(
                "jav-999",
                "Java 01",
                45,
                3.0,
                "Computer Science"
        );

        CourseResponseModel updatedCourseResponse = new CourseResponseModel(
                EXISTING_COURSE_ID,
                "jav-02",
                "Java 02",
                40,
                3.0,
                "Computer Science"
        );

        when(courseService.updateCourse(any(Mono.class), eq(EXISTING_COURSE_ID)))
                .thenReturn(Mono.just(updatedCourseResponse));


        Mono<ResponseEntity<CourseResponseModel>> result = courseController.updateCourse(EXISTING_COURSE_ID, Mono.just(request));

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    CourseResponseModel response = responseEntity.getBody();
                    assertNotNull(response);
                    assertEquals("jav-02", response.courseNumber());
                    assertEquals(EXISTING_COURSE_ID, response.courseId());
                    assertEquals("Java 02", response.courseName());
                    assertEquals(40, response.numHours());
                    assertEquals(3.0, response.numCredits());
                    assertEquals("Computer Science", response.department());
                    assertEquals(200, responseEntity.getStatusCode().value());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenDeleteCourse_withValidId_thenReturnDeletedCourse() {
        CourseResponseModel CourseResponse = new CourseResponseModel(
                EXISTING_COURSE_ID,
                "jav-01",
                "Java 01",
                40,
                3.0,
                "Computer Science"
        );

        when(courseService.deleteCourse(EXISTING_COURSE_ID))
                .thenReturn(Mono.just(CourseResponse));

        // Act
        Mono<ResponseEntity<CourseResponseModel>> result = courseController.deleteCourse(EXISTING_COURSE_ID);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    CourseResponseModel response = responseEntity.getBody();
                    assertEquals(EXISTING_COURSE_ID, response.courseId());
                    assertEquals("jav-01", response.courseNumber());
                    assertEquals(200, responseEntity.getStatusCode().value());
                    return true;
                })
                .verifyComplete();

    }

    @Test
    public void whenUpdateCourse_withInvalidCourseId_thenThrowInvalidCourseIdException() {
        CourseRequestModel courseRequestModel = new CourseRequestModel(
                "cat-423",
                "Web Services Testing",
                45,
                3.0,
                "Computer Science"
        );

        // Act
        Mono<ResponseEntity<CourseResponseModel>> result =
                courseController.updateCourse(INVALID_COURSE_ID, Mono.just(courseRequestModel));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidCourseIdException
                        && e.getMessage().equals("Course id=" + INVALID_COURSE_ID + " is invalid"))
                .verify();
    }

    @Test
    public void whenDeleteCourse_withInvalidCourseId_thenThrowInvalidCourseIdException() {
        // Act
        Mono<ResponseEntity<CourseResponseModel>> result =
                courseController.deleteCourse(INVALID_COURSE_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidCourseIdException
                        && e.getMessage().equals("Course id=" + INVALID_COURSE_ID + " is invalid"))
                .verify();
    }

    @Test
    public void whenAddCourse_withNullCredits_thenThrowInvalidInputException() {
        CourseRequestModel request = new CourseRequestModel(
                "jav-123",
                "Java Null Credits",
                40,
                null,
                "Computer Science"
        );

        RequestValidator validator = new RequestValidator();

        Mono<CourseRequestModel> result = RequestValidator.validateBody().apply(Mono.just(request));

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException
                        && e.getMessage().equals("Course credits must be greater than 0"))
                .verify();
    }

    @Test
    public void whenAddCourse_withZeroCredits_thenThrowInvalidInputException() {
        CourseRequestModel request = new CourseRequestModel(
                "jav-101",
                "Intro to Java",
                40,
                0.0,
                "Computer Science"
        );

        Mono<ResponseEntity<CourseResponseModel>> result = courseController.addCourse(Mono.just(request));

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException
                        && e.getMessage().equals("Course credits must be greater than 0"))
                .verify();
    }

    @Test
    public void whenAddCourse_withNullHours_thenThrowInvalidInputException() {
        CourseRequestModel request = new CourseRequestModel(
                "jav-101",
                "Intro to Java",
                null,
                3.0,
                "Computer Science"
        );

        Mono<ResponseEntity<CourseResponseModel>> result = courseController.addCourse(Mono.just(request));

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException
                        && e.getMessage().equals("Course hours must be greater than 0"))
                .verify();
    }

}