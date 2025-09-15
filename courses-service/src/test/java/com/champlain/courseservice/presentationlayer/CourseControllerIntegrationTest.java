package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.exceptionhandling.HttpErrorInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourseControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CourseRepository courseRepository;

    private final Long dbSize = 1000L;
    private String existingCourseId;
    private Course existingCourse;

    @BeforeEach
    public void dbSetup() {
        StepVerifier
                .create(courseRepository.count())
                .consumeNextWith(count -> {
                    assertEquals(dbSize, count);
                })
                .verifyComplete();
    }

    @Test
    public void getAllCoursesEventStream() {
        webTestClient.get()
                .uri("/api/v1/courses")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CourseRequestModel.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(dbSize)
                .verifyComplete();
    }

    @Test
    public void whenGetCourseByCourseId_withExistingCourseId_thenReturnCourseResponseModel() {

        // Arrange
        Mono.from(courseRepository.findAll()
                        .take(1))
                .doOnNext(course -> {
                    existingCourse = course;
                    existingCourseId = course.getCourseId();
                })
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // Act and assert
        webTestClient.get()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.courseId").isEqualTo(existingCourse.getCourseId())
                .jsonPath("$.courseName").isEqualTo(existingCourse.getCourseName())
                .jsonPath("$.numHours").isEqualTo(existingCourse.getNumHours())
                .jsonPath("$.numCredits").isEqualTo(existingCourse.getNumCredits())
                .jsonPath("$.department").isEqualTo(existingCourse.getDepartment());
    }

    @Test
    public void whenNewCourse_withValidRequestBody_shouldReturnSuccess() {
        CourseRequestModel courseRequestModel = new CourseRequestModel(
                "cat-423",
                "Web Services Testing",
                45,
                3.0,
                "Computer Science"
        );

        webTestClient
                .post()
                .uri("/api/v1/courses")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseRequestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CourseResponseModel.class)
                .value(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertNotNull(courseResponseModel.courseId());
                    assertNotNull(courseResponseModel.courseName());
                    assertNotNull(courseResponseModel.numHours());
                    assertNotNull(courseResponseModel.numCredits());
                    assertNotNull(courseResponseModel.department());
                });
    }

    @Test
    public void whenAddNewCourse_withMissingCourseName_shouldReturnUnProcessableEntity() {

        var courseRequestModel = this.resourceToString("courseRequestModel-missing-courseName-422.json");

        webTestClient.post()
                .uri("/api/v1/courses")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals("Course name is required", errorInfo.getMessage());
                });
    }

    @Test
    public void whenUpdateCourse_withValidRequestBody_thenReturnUpdatedCourse() {
        Mono.from(courseRepository.findAll().take(1))
                .doOnNext(course -> {
                    existingCourse = course;
                    existingCourseId = course.getCourseId();
                })
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        CourseRequestModel updatedRequest = new CourseRequestModel(
                "cat-500",
                "Math 01",
                50,
                4.0,
                "Math"
        );

        webTestClient.put()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseModel.class)
                .value(updatedResponse -> {
                    assertNotNull(updatedResponse);
                    assertEquals(existingCourseId, updatedResponse.courseId());
                    assertEquals("Math 01", updatedResponse.courseName());
                    assertEquals(50, updatedResponse.numHours());
                    assertEquals(4.0, updatedResponse.numCredits());
                    assertEquals("Math", updatedResponse.department());
                });
    }

    @Test
    public void whenDeleteCourse_withExistingCourseId_thenReturnNoContent() {
        Mono.from(courseRepository.findAll().take(1))
                .doOnNext(course -> existingCourseId = course.getCourseId())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        webTestClient.delete()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void whenGetCourseByCourseId_withNonExistingCourseId_thenReturnNotFound() {
        String nonExistingCourseId = "123e4567-e89b-12d3-a456-426614174000";

        webTestClient.get()
                .uri("/api/v1/courses/{courseId}", nonExistingCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals("Course with id=" + nonExistingCourseId + " is not found", errorInfo.getMessage());
                });
    }

    @Test
    public void whenUpdateCourse_withInvalidCourseId_thenReturnBadRequest() {
        String invalidCourseId = "123";

        CourseRequestModel updatedRequest = new CourseRequestModel(
                "phy-637",
                "Physics 01",
                45,
                3.0,
                "Physics"
        );

        webTestClient.put()
                .uri("/api/v1/courses/{courseId}", invalidCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals("Course id=" + invalidCourseId + " is invalid", errorInfo.getMessage());
                });
    }

    @Test
    public void whenDeleteCourse_withNonExistingCourseId_thenReturnNotFound() {
        String nonExistingCourseId = "123e4567-e89b-12d3-a456-426614174000";

        webTestClient.delete()
                .uri("/api/v1/courses/{courseId}", nonExistingCourseId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals("Course with id=" + nonExistingCourseId + " is not found", errorInfo.getMessage());
                });
    }


    protected String resourceToString(String relativePath) {
        final Path TEST_RESOURCES_PATH = Path.of("src/test/java/resources");

        try {
            return Files.readString(TEST_RESOURCES_PATH.resolve(relativePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}