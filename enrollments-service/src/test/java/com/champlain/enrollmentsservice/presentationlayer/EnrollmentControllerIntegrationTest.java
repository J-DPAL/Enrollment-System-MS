package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.TestData;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnrollmentControllerIntegrationTest extends AbstractIntegrationClass {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestData testData = new TestData();


    @Test
    @Order(1)
    public void whenAddEnrollment_withNonExistingCourseId_thenReturnNotFound() throws JsonProcessingException {
        mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
        mockGetCourseByCourseIdException(TestData.NON_EXISTING_COURSEID, 404);

        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment_withNonExistingCourseId_RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course with id=" + TestData.NON_EXISTING_COURSEID + " is not found");

        StepVerifier.create(enrollmentRepository.count())
                .expectNext(testData.dbSize)
                .verifyComplete();
    }

    @Test
    @Order(2)
    public void whenAddValidEnrollment_thenReturnEnrollmentResponseModel() throws JsonProcessingException {
        mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
        mockGetCourseByCourseIdSuccess(testData.course1ResponseModel);

        webTestClient.post()
                .uri("/api/v1/enrollments")
                .body(Mono.just(testData.enrollment1RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentResponseModel.class)
                .value(enrollmentResponseModel -> {
                    assertNotNull(enrollmentResponseModel.enrollmentId());
                    assertEquals(testData.enrollment1RequestModel.enrollmentYear(), enrollmentResponseModel.enrollmentYear());
                    assertEquals(testData.enrollment1RequestModel.semester(), enrollmentResponseModel.semester());
                    assertEquals(testData.enrollment1RequestModel.studentId(), enrollmentResponseModel.studentId());
                    assertEquals(testData.enrollment1RequestModel.courseId(), enrollmentResponseModel.courseId());
                });

        StepVerifier.create(enrollmentRepository.count())
                .expectNext(testData.dbSize + 1)
                .verifyComplete();
    }


    @Test
    @Order(3)
    public void whenGetAllEnrollments_thenReturnEnrollments() {
        webTestClient.get()
                .uri("/api/v1/enrollments")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(EnrollmentResponseModel.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(testData.dbSize)
                .thenCancel()
                .verify();
    }



    @Test
    @Order(4)
    public void whenGetEnrollmentById_withExistingId_thenReturnEnrollment() {
        String existingId = testData.enrollment1.getEnrollmentId();

        webTestClient.get()
                .uri("/api/v1/enrollments/{enrollmentId}", existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentResponseModel.class)
                .value(enrollment -> assertEquals(existingId, enrollment.enrollmentId()));
    }

    @Test
    @Order(5)
    public void whenGetEnrollmentById_withNonExistingId_thenReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/enrollments/{enrollmentId}", TestData.NON_EXISTING_ENROLLMENTID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Enrollment with id=" + TestData.NON_EXISTING_ENROLLMENTID + " is not found");
    }


    @Test
    @Order(6)
    public void whenUpdateEnrollment_withValidData_thenReturnUpdatedEnrollment() throws JsonProcessingException {
        String existingId = testData.enrollment1.getEnrollmentId();

        mockGetStudentByStudentIdSuccess(testData.student2ResponseModel);
        mockGetCourseByCourseIdSuccess(testData.course2ResponseModel);

        webTestClient.put()
                .uri("/api/v1/enrollments/{enrollmentId}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment2RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentResponseModel.class)
                .value(enrollment -> {
                    assertEquals(existingId, enrollment.enrollmentId());
                    assertEquals(testData.enrollment2RequestModel.enrollmentYear(), enrollment.enrollmentYear());
                    assertEquals(testData.enrollment2RequestModel.semester(), enrollment.semester());
                    assertEquals(testData.enrollment2RequestModel.studentId(), enrollment.studentId());
                    assertEquals(testData.enrollment2RequestModel.courseId(), enrollment.courseId());
                });
    }

    @Test
    @Order(7)
    public void whenUpdateEnrollment_withNonExistingId_thenReturnNotFound() {
        webTestClient.put()
                .uri("/api/v1/enrollments/{enrollmentId}", TestData.NON_EXISTING_ENROLLMENTID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment2RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Enrollment with id=" + TestData.NON_EXISTING_ENROLLMENTID + " is not found");
    }


    @Test
    @Order(8)
    public void whenDeleteEnrollment_withExistingId_thenReturnIsOk() {
        String existingId = testData.enrollment1.getEnrollmentId();

        webTestClient.delete()
                .uri("/api/v1/enrollments/{enrollmentId}", existingId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @Order(9)
    public void whenDeleteEnrollment_withNonExistingId_thenReturnNotFound() {
        webTestClient.delete()
                .uri("/api/v1/enrollments/{enrollmentId}", TestData.NON_EXISTING_ENROLLMENTID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Enrollment with id=" + TestData.NON_EXISTING_ENROLLMENTID + " is not found");
    }


    @Test
    @Order(10)
    public void whenAddEnrollment_withInvalidStudentId_thenReturn() throws JsonProcessingException {
        mockGetStudentByStudentIdException(TestData.INVALID_STUDENTID, 422);
        mockGetCourseByCourseIdSuccess(testData.course1ResponseModel);

        webTestClient.post()
                .uri("/api/v1/enrollments")
                .body(Mono.just(testData.enrollment_withInvalidStudentId_RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Student id=" + TestData.INVALID_STUDENTID + " is invalid");
    }

    @Test
    @Order(11)
    public void whenAddEnrollment_withInvalidCourseId_thenReturnInvalidIdException() throws JsonProcessingException {
        mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
        mockGetCourseByCourseIdException(TestData.INVALID_COURSEID, 422);

        webTestClient.post()
                .uri("/api/v1/enrollments")
                .body(Mono.just(testData.enrollment_withInvalidCourseId_RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course id=" + TestData.INVALID_COURSEID + " is invalid");
    }

    private void mockGetCourseByCourseIdSuccess(com.champlain.enrollmentsservice.domainclientlayer.courses.CourseResponseModel model) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(model);

        mockServerClient.when(org.mockserver.model.HttpRequest.request("/api/v1/courses/" + model.courseId()))
                .respond(org.mockserver.model.HttpResponse.response(jsonBody)
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON));
    }

    private void mockGetCourseByCourseIdException(String courseId, int responseCode) {
        mockServerClient.when(org.mockserver.model.HttpRequest.request("/api/v1/courses/" + courseId))
                .respond(org.mockserver.model.HttpResponse.response()
                        .withStatusCode(responseCode)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON));
    }

    private void mockGetStudentByStudentIdSuccess(com.champlain.enrollmentsservice.domainclientlayer.students.StudentResponseModel model) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(model);

        mockServerClient.when(org.mockserver.model.HttpRequest.request("/api/v1/students/" + model.studentId()))
                .respond(org.mockserver.model.HttpResponse.response(jsonBody)
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON));
    }

    private void mockGetStudentByStudentIdException(String studentId, int responseCode) {
        mockServerClient.when(org.mockserver.model.HttpRequest.request("/api/v1/students/" + studentId))
                .respond(org.mockserver.model.HttpResponse.response()
                        .withStatusCode(responseCode)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON));
    }
}
