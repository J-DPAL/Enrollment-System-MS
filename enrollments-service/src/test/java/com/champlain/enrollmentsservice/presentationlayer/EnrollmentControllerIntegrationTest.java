package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.TestData;
import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseResponseModel;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentResponseModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnrollmentControllerIntegrationTest extends AbstractIntegrationClass {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Non-mutating tests first

    @Test
    @Order(1)
    public void whenAddEnrollment_withNonExistingCourseId_thenReturnNotFound() {
        // Arrange
        try {
            mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
            mockGetCourseByCourseIdException(TestData.NON_EXISTING_COURSEID, 404);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment_withNonExistingCourseId_RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course with id=" + TestData.NON_EXISTING_COURSEID + " is not found");

        StepVerifier.create(enrollmentRepository.count())
                .expectNext(testData.dbSize)
                .verifyComplete();
    }

    // Mutating tests at the end

    @Test
    @Order(2)
    public void whenAddValidEnrollment_thenReturnEnrollmentResponseModel() {
        // Arrange
        try {
            mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
            mockGetCourseByCourseIdSuccess(testData.course1ResponseModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Act
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .body(Mono.just(testData.enrollment1RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .value(enrollmentResponseModel -> {
                    assertNotNull(enrollmentResponseModel);
                    assertNotNull(enrollmentResponseModel.enrollmentId());
                    assertEquals(testData.enrollment1RequestModel.enrollmentYear(), enrollmentResponseModel.enrollmentYear());
                    // Add the rest of the fields
                });

        StepVerifier.create(enrollmentRepository.count())
                .expectNext(testData.dbSize + 1)
                .verifyComplete();

    }

    private void mockGetCourseByCourseIdSuccess(CourseResponseModel model) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(model);

        mockServerClient.when(HttpRequest.request("/api/v1/courses/" + model.courseId()))
                .respond(HttpResponse.response(jsonBody)
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)); // Must be from mockserver model library
    }

    private void mockGetCourseByCourseIdException(String courseId, int responseCode) {
        mockServerClient.when(HttpRequest.request("/api/v1/courses/" + courseId))
                .respond(
                        HttpResponse.response()
                                .withStatusCode(responseCode)
                                .withContentType(MediaType.APPLICATION_JSON) // Must be from mockserver model library
                );
    }

    private void mockGetStudentByStudentIdSuccess(StudentResponseModel model) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(model);

        mockServerClient.when(HttpRequest.request("/api/v1/students/" + model.studentId()))
                .respond(HttpResponse.response(jsonBody)
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)); // Must be from mockserver model library
    }

    private void mockGetStudentByStudentIdException(String studentId, int responseCode) {
        mockServerClient.when(HttpRequest.request("/api/v1/students/" + studentId))
                .respond(
                        HttpResponse.response()
                                .withStatusCode(responseCode)
                                .withContentType(MediaType.APPLICATION_JSON) // Must be from mockserver model library
                );
    }

}