package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import com.champlain.enrollmentsservice.validation.RequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping(
            value = "",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<EnrollmentResponseModel> getEnrollments() {
        return enrollmentService.getEnrollments();
    }

    @GetMapping("{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponseModel>> getEnrollmentByEnrollmentId(@PathVariable String enrollmentId) {
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidEnrollmentId(enrollmentId))
                .flatMap(enrollmentService::getEnrollmentByEnrollmentId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.enrollmentNotFound(enrollmentId));
    }

    @PostMapping()
    public Mono<ResponseEntity<EnrollmentResponseModel>> addEnrollment(@RequestBody Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return enrollmentRequestModel
                .as(enrollmentService::addEnrollment)
                .map(e-> ResponseEntity.status(HttpStatus.CREATED).body(e));
    }

    @PutMapping("{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponseModel>> updateEnrollment(@PathVariable String enrollmentId,
                                                                  @RequestBody Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidEnrollmentId(enrollmentId))
                .thenReturn(enrollmentRequestModel.transform(RequestValidator.validateBody()))
                .flatMap(validReq -> enrollmentService.updateEnrollment(validReq, enrollmentId))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.enrollmentNotFound(enrollmentId));
    }

    @DeleteMapping("{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponseModel>> deleteEnrollment(@PathVariable String enrollmentId) {
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidEnrollmentId(enrollmentId))
                .flatMap(enrollmentService::deleteEnrollment)
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.enrollmentNotFound(enrollmentId));
    }
}
