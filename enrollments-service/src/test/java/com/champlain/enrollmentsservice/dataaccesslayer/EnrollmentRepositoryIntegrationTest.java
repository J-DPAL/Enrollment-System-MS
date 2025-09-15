package com.champlain.enrollmentsservice.dataaccesslayer;

import com.champlain.enrollmentsservice.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class EnrollmentRepositoryIntegrationTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final TestData testData = new TestData();

    @BeforeEach
    public void setUpDB() {
        StepVerifier
                .create(enrollmentRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenFindAllEnrollments_thenReturnAllEnrollments() {
        StepVerifier.create(enrollmentRepository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void whenFindEnrollmentByEnrollmentId_withExistingId_thenReturnEnrollment() {
        StepVerifier
                .create(enrollmentRepository.save(testData.enrollment1))
                .consumeNextWith(inserted -> {
                    assertNotNull(inserted);
                    assertEquals(testData.enrollment1.getEnrollmentId(), inserted.getEnrollmentId());
                })
                .verifyComplete();

        StepVerifier
                .create(enrollmentRepository.findEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId()))
                .consumeNextWith(found -> {
                    assertNotNull(found);
                    assertEquals(testData.enrollment1.getEnrollmentId(), found.getEnrollmentId());
                })
                .verifyComplete();
    }

    @Test
    public void whenFindEnrollmentByEnrollmentId_withNonExistingId_thenReturnEmptyMono() {
        StepVerifier
                .create(enrollmentRepository.findEnrollmentByEnrollmentId(TestData.NON_EXISTING_ENROLLMENTID))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void whenEnrollmentIsValid_thenAddEnrollmentToDB_thenReturnOne() {
        StepVerifier
                .create(enrollmentRepository.save(testData.enrollment1))
                .consumeNextWith(inserted -> {
                    assertNotNull(inserted);
                    assertEquals(testData.enrollment1.getEnrollmentId(), inserted.getEnrollmentId());
                    assertEquals(testData.enrollment1.getStudentId(), inserted.getStudentId());
                    assertEquals(testData.enrollment1.getCourseId(), inserted.getCourseId());
                })
                .verifyComplete();

        StepVerifier
                .create(enrollmentRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void whenEnrollmentIsValid_thenUpdateEnrollment() {
        StepVerifier
                .create(enrollmentRepository.save(testData.enrollment1))
                .expectNextCount(1)
                .verifyComplete();

        Enrollment updatedEnrollment = Enrollment.builder()
                .enrollmentId(testData.enrollment1.getEnrollmentId())
                .enrollmentYear(2023)
                .semester(Semester.SPRING)
                .studentId(testData.enrollment1.getStudentId())
                .studentFirstName(testData.enrollment1.getStudentFirstName())
                .studentLastName(testData.enrollment1.getStudentLastName())
                .courseId(testData.enrollment1.getCourseId())
                .courseNumber(testData.enrollment1.getCourseNumber())
                .courseName(testData.enrollment1.getCourseName())
                .build();


        StepVerifier
                .create(enrollmentRepository.save(updatedEnrollment))
                .consumeNextWith(saved -> {
                    assertNotNull(saved);
                    assertEquals(updatedEnrollment.getEnrollmentId(), saved.getEnrollmentId());
                    assertEquals(2023, saved.getEnrollmentYear());
                    assertEquals(Semester.SPRING, saved.getSemester());
                })
                .verifyComplete();
    }

    @Test
    public void whenEnrollmentIsValid_thenDeleteEnrollment() {
        StepVerifier
                .create(enrollmentRepository.save(testData.enrollment1))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(enrollmentRepository.delete(testData.enrollment1))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier
                .create(enrollmentRepository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void whenFindEnrollmentByInvalidId_thenReturnEmptyMono() {
        StepVerifier
                .create(enrollmentRepository.findEnrollmentByEnrollmentId(TestData.INVALID_ENROLLMENTID))
                .expectNextCount(0)
                .verifyComplete();
    }
}
