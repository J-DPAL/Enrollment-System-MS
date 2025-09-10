package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseServiceClient;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentServiceClientAsynchronous;
import com.champlain.enrollmentsservice.mapper.EntityModelMapper;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import io.micrometer.core.ipc.http.HttpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    private final StudentServiceClientAsynchronous studentClient;

    private final CourseServiceClient courseClient;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, StudentServiceClientAsynchronous studentClient, CourseServiceClient courseClient) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentClient = studentClient;
        this.courseClient = courseClient;
    }

    @Override
    public Flux<EnrollmentResponseModel> getEnrollments() {
        return enrollmentRepository.findAll().map(EntityModelMapper::toModel);
    }

    @Override
    public Mono<EnrollmentResponseModel> addEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return enrollmentRequestModel
                .map(RequestContext::new)
                .flatMap(this::studentRequestResponse)
                .flatMap(this::courseRequestResponse)
                .map(EntityModelMapper::toEntity)
                .flatMap(enrollmentRepository::save)
                .map(EntityModelMapper::toModel);


    }

    @Override
    public Mono<EnrollmentResponseModel> getEnrollmentByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .doOnNext(enrollment -> log.info("Enrollment found with Id: {}", enrollment.getId()))
                .map(EntityModelMapper::toModel);
    }

    @Override
    public Mono<EnrollmentResponseModel> updateEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel, String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .flatMap(foundId -> enrollmentRequestModel
                        .map(RequestContext::new)
                        .flatMap(this::studentRequestResponse)
                        .flatMap(this::courseRequestResponse)
                        .map(EntityModelMapper::toEntity)
                        .doOnNext(e -> e.setEnrollmentId(enrollmentId))
                        .doOnNext(e -> e.setId(foundId.getId()))
                )
                .flatMap(enrollmentRepository::save)
                .map(EntityModelMapper::toModel);
    }

    @Override
    public Mono<EnrollmentResponseModel> deleteEnrollment(String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .flatMap(found -> enrollmentRepository.delete(found)
                        .then(Mono.just(found)))
                .map(EntityModelMapper::toModel);
    }

    private Mono<RequestContext> studentRequestResponse(RequestContext requestContext) {
        return studentClient.getStudentByStudentId(requestContext.getEnrollmentRequestModel().studentId())
                .doOnNext(requestContext::setStudentResponseModel)
                .thenReturn(requestContext);
    }

    private Mono<RequestContext> courseRequestResponse(RequestContext requestContext) {
        return courseClient.getCourseByCourseId(requestContext.getEnrollmentRequestModel().courseId())
                .doOnNext(requestContext::setCourseResponseModel)
                .thenReturn(requestContext);
    }
}