package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.TestData;
import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseServiceClient;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentServiceClientAsynchronous;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceUnitTest {

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentServiceClientAsynchronous studentClient;

    @Mock
    private CourseServiceClient courseClient;

    private final TestData testData = new TestData();

    @BeforeEach
    void setUp() {
        // I used lenient to avoid unnecessary stubbing exceptions
        // Reminder to check later if I can find a different solution to avoid this error
        lenient().when(studentClient.getStudentByStudentId(testData.student1ResponseModel.studentId()))
                .thenReturn(Mono.just(testData.student1ResponseModel));
        lenient().when(studentClient.getStudentByStudentId(testData.student2ResponseModel.studentId()))
                .thenReturn(Mono.just(testData.student2ResponseModel));
        lenient().when(courseClient.getCourseByCourseId(testData.course1ResponseModel.courseId()))
                .thenReturn(Mono.just(testData.course1ResponseModel));
        lenient().when(courseClient.getCourseByCourseId(testData.course2ResponseModel.courseId()))
                .thenReturn(Mono.just(testData.course2ResponseModel));

        lenient().when(studentClient.getStudentByStudentId(anyString()))
                .thenAnswer(invocation -> {
                    String id = invocation.getArgument(0);
                    if (id.equals(testData.student1ResponseModel.studentId()))
                        return Mono.just(testData.student1ResponseModel);
                    if (id.equals(testData.student2ResponseModel.studentId()))
                        return Mono.just(testData.student2ResponseModel);
                    return Mono.empty();
                });

        lenient().when(courseClient.getCourseByCourseId(anyString()))
                .thenAnswer(invocation -> {
                    String id = invocation.getArgument(0);
                    if (id.equals(testData.course1ResponseModel.courseId()))
                        return Mono.just(testData.course1ResponseModel);
                    if (id.equals(testData.course2ResponseModel.courseId()))
                        return Mono.just(testData.course2ResponseModel);
                    return Mono.empty();
                });
    }

    @Test
    void whenGetAllEnrollments_thenReturnAll() {
        when(enrollmentRepository.findAll())
                .thenReturn(Flux.just(testData.enrollment1, testData.enrollment2));

        StepVerifier.create(enrollmentService.getEnrollments())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void whenGetEnrollmentById_withExistingId_thenReturnEnrollment() {
        when(enrollmentRepository.findEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId()))
                .thenReturn(Mono.just(testData.enrollment1));

        StepVerifier.create(enrollmentService.getEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId()))
                .expectNextMatches(enrollment -> enrollment.enrollmentId().equals(testData.enrollment1.getEnrollmentId()))
                .verifyComplete();
    }

    @Test
    void whenGetEnrollmentById_withNonExistingId_thenReturnEmpty() {
        when(enrollmentRepository.findEnrollmentByEnrollmentId(TestData.NON_EXISTING_ENROLLMENTID))
                .thenReturn(Mono.empty());

        StepVerifier.create(enrollmentService.getEnrollmentByEnrollmentId(TestData.NON_EXISTING_ENROLLMENTID))
                .expectComplete()
                .verify();
    }

    @Test
    void whenAddEnrollment_thenReturnEnrollment() {
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(Mono.just(testData.enrollment1));

        StepVerifier.create(enrollmentService.addEnrollment(Mono.just(testData.enrollment1RequestModel)))
                .expectNextMatches(enrollment -> enrollment.enrollmentId().equals(testData.enrollment1.getEnrollmentId()))
                .verifyComplete();
    }

    @Test
    void whenUpdateEnrollment_thenReturnUpdatedEnrollment() {
        EnrollmentRequestModel updateRequest = testData.enrollment2RequestModel;

        when(enrollmentRepository.findEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId()))
                .thenReturn(Mono.just(testData.enrollment1));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(enrollmentService.updateEnrollment(Mono.just(updateRequest), testData.enrollment1.getEnrollmentId()))
                .expectNextMatches(enrollment -> enrollment.enrollmentYear() == updateRequest.enrollmentYear() &&
                        enrollment.semester() == updateRequest.semester())
                .verifyComplete();
    }

    @Test
    void whenDeleteEnrollment_thenReturnDeletedEnrollment() {
        when(enrollmentRepository.findEnrollmentByEnrollmentId(testData.enrollment1.getEnrollmentId()))
                .thenReturn(Mono.just(testData.enrollment1));
        when(enrollmentRepository.delete(testData.enrollment1)).thenReturn(Mono.empty());

        StepVerifier.create(enrollmentService.deleteEnrollment(testData.enrollment1.getEnrollmentId()))
                .expectNextMatches(enrollment -> enrollment.enrollmentId().equals(testData.enrollment1.getEnrollmentId()))
                .verifyComplete();
    }
}
