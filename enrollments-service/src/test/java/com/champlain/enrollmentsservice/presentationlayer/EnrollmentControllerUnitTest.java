package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.TestData;
import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.InvalidEnrollmentIdException;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.StudentNotFoundException;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.champlain.enrollmentsservice.mapper.EntityModelMapper.toModel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerUnitTest {

    @InjectMocks
    private EnrollmentController enrollmentController;

    @Mock
    private EnrollmentService enrollmentService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        testData = new TestData();
    }

    // ---------------------- GET ALL ----------------------
    @Test
    void whenGetAllEnrollments_thenReturnAllEnrollments() {
        when(enrollmentService.getEnrollments())
                .thenReturn(Flux.just(
                        toModel(testData.enrollment1),
                        toModel(testData.enrollment2)
                ));

        Flux<EnrollmentResponseModel> result = enrollmentController.getEnrollments();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    // ---------------------- GET BY ID ----------------------
    @Test
    void whenGetEnrollmentById_withExistingId_thenReturnEnrollment() {
        when(enrollmentService.getEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId()))
                .thenReturn(Mono.just(toModel(testData.enrollment1)));

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.getEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getBody().enrollmentId().equals(testData.enrollment1.getEnrollmentId()))
                .verifyComplete();
    }

    // Negative test: invalid ID
    @Test
    void whenGetEnrollmentById_withInvalidId_thenThrowException() {
        String invalidId = "Enrollment123";

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.getEnrollmentByEnrollmentId(invalidId);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidEnrollmentIdException
                        && e.getMessage().equals("Enrollment id=" + invalidId + " is invalid"))
                .verify();
    }

    // ---------------------- ADD ----------------------
    @Test
    void whenAddEnrollment_withValidData_thenReturnCreatedEnrollment() {
        when(enrollmentService.addEnrollment(any(Mono.class)))
                .thenReturn(Mono.just(toModel(testData.enrollment1)));

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.addEnrollment(Mono.just(testData.enrollment1RequestModel));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().value() == 201 &&
                        response.getBody().enrollmentId().equals(testData.enrollment1.getEnrollmentId()))
                .verifyComplete();
    }

    // Negative test: non-existing student
    @Test
    void whenAddEnrollment_withNonExistingStudent_thenReturnEmpty() {
        when(enrollmentService.addEnrollment(any(Mono.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.addEnrollment(Mono.just(testData.enrollment_withNonExistingStudentId_RequestModel));

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    // Negative test: non-existing course
    @Test
    void whenAddEnrollment_withNonExistingCourse_thenReturnEmpty() {
        when(enrollmentService.addEnrollment(any(Mono.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.addEnrollment(Mono.just(testData.enrollment_withNonExistingCourseId_RequestModel));

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    // ---------------------- UPDATE ----------------------
    @Test
    void whenUpdateEnrollment_withExistingId_thenReturnUpdatedEnrollment() {
        when(enrollmentService.updateEnrollment(any(Mono.class), any(String.class)))
                .thenReturn(Mono.just(toModel(testData.enrollment2)));

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.updateEnrollment(testData.enrollment2.getEnrollmentId(),
                        Mono.just(testData.enrollment2RequestModel));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().value() == 200 &&
                        response.getBody().enrollmentId().equals(testData.enrollment2.getEnrollmentId()))
                .verifyComplete();
    }

    // ---------------------- DELETE ----------------------
    @Test
    void whenDeleteEnrollment_withExistingId_thenReturnDeletedEnrollment() {
        when(enrollmentService.deleteEnrollment(testData.enrollment1.getEnrollmentId()))
                .thenReturn(Mono.just(toModel(testData.enrollment1)));

        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.deleteEnrollment(testData.enrollment1.getEnrollmentId());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().value() == 200 &&
                        response.getBody().enrollmentId().equals(testData.enrollment1.getEnrollmentId()))
                .verifyComplete();
    }

    @Test
    void whenAddEnrollment_withNonExistingStudent_thenThrowStudentNotFoundException() {
        // Arrange: simulate the service returning an error for non-existing student
        when(enrollmentService.addEnrollment(any(Mono.class)))
                .thenReturn(Mono.error(new StudentNotFoundException(TestData.NON_EXISTING_STUDENTID)));

        // Act
        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.addEnrollment(Mono.just(testData.enrollment_withNonExistingStudentId_RequestModel));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof StudentNotFoundException &&
                        e.getMessage().equals("Student with id=" + TestData.NON_EXISTING_STUDENTID + " is not found"))
                .verify();
    }

}
