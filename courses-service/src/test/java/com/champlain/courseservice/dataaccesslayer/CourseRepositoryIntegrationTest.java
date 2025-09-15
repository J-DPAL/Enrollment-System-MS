package com.champlain.courseservice.dataaccesslayer;


import com.champlain.courseservice.exceptionhandling.exceptions.CourseNotFoundException;
import com.champlain.courseservice.exceptionhandling.exceptions.InvalidCourseIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@ActiveProfiles("test")
class CourseRepositoryIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    public void setUpDB() {
        StepVerifier
                .create(courseRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void whenFindCourseByCourseId_withExistingValidCourseId_thenReturnCourse() {
        //arrange
        String courseId = UUID.randomUUID().toString();
        Course course = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web-Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier
                .create(courseRepository.save(course))
                .consumeNextWith(insertedCourse -> {
                    assertNotNull(insertedCourse);
                    assertEquals(course.getCourseId(), insertedCourse.getCourseId());
                })
                .verifyComplete();

        //act and assert
        StepVerifier
                .create(courseRepository.findCourseByCourseId(courseId))
                .consumeNextWith(foundCourse -> {
                    assertNotNull(foundCourse);
                    assertEquals(courseId, foundCourse.getCourseId());
                })
                .verifyComplete();

    }

    @Test
    public void whenFindCourseByCourseId_withNonExistingCourseId_thenReturnEmptyMono() {
        String nonExistingCourseId = UUID.randomUUID().toString();

        StepVerifier
                .create(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .expectNextCount(0)
                .verifyComplete();

    }

    @Test
    public void whenSaveCourseWithDuplicateCourseId_thenThrowDuplicateKeyException() {
        //arrange
        String courseId = UUID.randomUUID().toString();
        Course course1 = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web-Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        Course course2 = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-423")
                .courseName("Web Services 2")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier
                .create(courseRepository.save(course1))
                .consumeNextWith(insertedCourse -> {
                    assertNotNull(insertedCourse);
                    assertEquals(course1.getCourseId(), insertedCourse.getCourseId());
                })
                .verifyComplete();

        //act and assert
        StepVerifier
                .create(courseRepository.save(course2))
                .verifyError(DuplicateKeyException.class);
    }

    @Test
    public void whenCourseEntityIsValid_thenAddCourseToDB_thenReturnOneCourse() {
        String courseId = UUID.randomUUID().toString();
        Course course = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web-Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier
                .create(courseRepository.save(course))
                .consumeNextWith(insertedCourse -> {
                    assertNotNull(insertedCourse);
                    assertEquals(course.getCourseId(), insertedCourse.getCourseId());
                    assertEquals(course.getCourseNumber(), insertedCourse.getCourseNumber());
                    assertEquals(course.getCourseName(), insertedCourse.getCourseName());
                    assertEquals(course.getNumHours(), insertedCourse.getNumHours());
                    assertEquals(course.getDepartment(), insertedCourse.getDepartment());
                })
                .verifyComplete();

        StepVerifier
                .create(courseRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    public void whenCourseEntityIsValid_thenUpdateCourse() {
        String courseId = UUID.randomUUID().toString();
        Course course = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web-Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier
                .create(courseRepository.save(course))
                .consumeNextWith(insertedCourse -> {
                    assertNotNull(insertedCourse);
                    assertEquals(course.getCourseId(), insertedCourse.getCourseId());
                    assertEquals(course.getCourseNumber(), insertedCourse.getCourseNumber());
                    assertEquals(course.getCourseName(), insertedCourse.getCourseName());
                    assertEquals(course.getNumHours(), insertedCourse.getNumHours());
                    assertEquals(course.getDepartment(), insertedCourse.getDepartment());
                })
                .verifyComplete();

        course.setCourseNumber("web-099");
        course.setCourseName("Web-Services-02");

        StepVerifier
                .create(courseRepository.save(course))
                .consumeNextWith(updatedCourse -> {
                    assertNotNull(updatedCourse);
                    assertNotNull(updatedCourse.getCourseId());
                    assertEquals(courseId, updatedCourse.getCourseId());
                    assertEquals("web-099", updatedCourse.getCourseNumber());
                    assertEquals("Web-Services-02", updatedCourse.getCourseName());
                    assertEquals(45, updatedCourse.getNumHours());
                    assertEquals(3.0, updatedCourse.getNumCredits());
                    assertEquals("Computer Science", updatedCourse.getDepartment());
                })
                .verifyComplete();

        StepVerifier
                .create(courseRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }


    @Test
    public void whenCourseEntityIsValid_thenDeleteCourse() {
        String courseId = UUID.randomUUID().toString();
        Course course = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web-Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier
                .create(courseRepository.save(course))
                .consumeNextWith(insertedCourse -> {
                    assertNotNull(insertedCourse);
                    assertEquals(course.getCourseId(), insertedCourse.getCourseId());
                    assertEquals(course.getCourseNumber(), insertedCourse.getCourseNumber());
                    assertEquals(course.getCourseName(), insertedCourse.getCourseName());
                    assertEquals(course.getNumHours(), insertedCourse.getNumHours());
                    assertEquals(course.getDepartment(), insertedCourse.getDepartment());
                })
                .verifyComplete();

        StepVerifier
                .create(courseRepository.delete(course))
                .expectNextCount(0)
                .verifyComplete();
    }

}