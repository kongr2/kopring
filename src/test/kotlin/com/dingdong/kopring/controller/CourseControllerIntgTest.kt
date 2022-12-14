package com.dingdong.kopring.controller

import com.dingdong.kopring.dto.CourseDto
import com.dingdong.kopring.entity.Course
import com.dingdong.kopring.repository.CourseRepository
import com.dingdong.kopring.repository.InstructorRepository
import com.dingdong.kopring.util.courseEntityList
import com.dingdong.kopring.util.instructorEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
internal class CourseControllerIntgTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var courseRepository: CourseRepository

    @Autowired
    lateinit var instructorRepository: InstructorRepository

    @BeforeEach
    fun setUp() {
        courseRepository.deleteAll()
        val instructor = instructorEntity()
        instructorRepository.save(instructor)
        val courses = courseEntityList(instructor)
        courseRepository.saveAll(courses)
    }

    @Test
    fun addCourse() {
        val instructor = instructorRepository.findAll().first()
        val courseDto = CourseDto(null, "test", "test", instructor.id)
        val result = webTestClient.post()
            .uri("/v1/courses", courseDto)
            .bodyValue(courseDto)
            .exchange()
            .expectStatus().isCreated
            .expectBody(CourseDto::class.java)
            .returnResult()
            .responseBody
        assertTrue(result!!.id != null)
    }

    @Test
    fun getCourseList(){
        val courseDtos = webTestClient.get()
            .uri("/v1/courses")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(CourseDto::class.java)
            .returnResult()
            .responseBody
        assertEquals(courseDtos!!.size, 3)
    }

    @Test
    fun getCourseListByName(){
        val uri = UriComponentsBuilder.fromUriString("/v1/courses")
            .queryParam("course_name", "test")
            .toUriString()

        val courseDtos = webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(CourseDto::class.java)
            .returnResult()
            .responseBody
        assertEquals(courseDtos!!.size, 3)
    }

    @Test
    fun updateCourse() {
        val instructor = instructorRepository.findAll().first()
        val course = Course(null, "test", "test", instructor)
        courseRepository.save(course)

        val updateCourseDto = CourseDto(null, "Hello test","Hello test", course.instructor!!.id)

        val result = webTestClient.put()
            .uri("/v1/courses/{course_id}", course.id)
            .bodyValue(updateCourseDto)
            .exchange()
            .expectStatus().isOk
            .expectBody(CourseDto::class.java)
            .returnResult()
            .responseBody

        assertEquals("Hello test", result!!.name)
    }

    @Test
    fun deleteCourse() {
        val instructor = instructorRepository.findAll().first()
        val course = Course(null, "test", "test", instructor)
        courseRepository.save(course)

        val result = webTestClient.delete()
            .uri("/v1/courses/{course_id}", course.id)
            .exchange()
            .expectStatus().isNoContent
    }

}