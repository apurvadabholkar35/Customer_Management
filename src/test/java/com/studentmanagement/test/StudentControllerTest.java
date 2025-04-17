package com.studentmanagement.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.StudentNotFoundException;
import com.studentmanagement.service.StudentService;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private StudentService studentService;

	private final Student student1 = new Student(1L, "John Doe", "john.doe@example.com", 85,
			LocalDate.now().minusMonths(5), "Merit");
	private final Student student2 = new Student(2L, "Jane Smith", "jane.smith@example.com", 92,
			LocalDate.now().minusYears(1), "Pass");
	private final Student studentWithoutId = new Student(null, "Peter Pan", "peter.pan@example.com", 78,
			LocalDate.now().minusMonths(2), null);
	private final Student studentWithInvalidEmail = new Student(null, "Invalid Email", "invalid-email", 78,
			LocalDate.now().minusMonths(2), null);

	@Test
	void getAllStudents_shouldReturnOk_withListOfStudents() throws Exception {
		List<Student> students = Arrays.asList(student1, student2);
		when(studentService.getAllStudents()).thenReturn(students);

		mockMvc.perform(get("/api/students")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].name").value(student1.getName()))
				.andExpect(jsonPath("$[1].email").value(student2.getEmail()));
	}

	@Test
	void getStudentById_shouldReturnOk_withStudent_whenIdExists() throws Exception {
		when(studentService.getStudentById(1L)).thenReturn(Optional.of(student1));

		mockMvc.perform(get("/api/students/1")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value(student1.getName()));
	}

	@Test
	void getStudentById_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
		when(studentService.getStudentById(3L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/students/3")).andExpect(status().isNotFound());
	}

	@Test
	void createStudent_shouldReturnCreated_withNewStudent_whenInputIsValid() throws Exception {
		Student createdStudent = new Student(3L, "Peter Pan", "peter.pan@example.com", 78,
				LocalDate.now().minusMonths(2), "Pass");
		when(studentService.createStudent(any(Student.class))).thenReturn(createdStudent);

		mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(studentWithoutId))).andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(3L))
				.andExpect(jsonPath("$.name").value(studentWithoutId.getName()))
				.andExpect(jsonPath("$.grade").value("Pass")); // Assuming grade calculation happens in service
	}

	@Test
	void createStudent_shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
		mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(studentWithInvalidEmail))).andExpect(status().isBadRequest());

	}

	@Test
	void updateStudent_shouldReturnOk_withUpdatedStudent_whenIdExistsAndInputIsValid() throws Exception {
		Student updatedStudent = new Student(1L, "John Updated", "john.updated@example.com", 88,
				LocalDate.now().minusMonths(3), "Merit");
		when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(updatedStudent);

		mockMvc.perform(put("/api/students/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedStudent))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value("John Updated")).andExpect(jsonPath("$.grade").value("Merit"));
	}

	@Test
	void updateStudent_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
		when(studentService.updateStudent(eq(4L), any(Student.class)))
				.thenThrow(new StudentNotFoundException("Student not found with id: 4"));

		mockMvc.perform(put("/api/students/4").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(student1))).andExpect(status().isNotFound());
	}

	@Test
	void deleteStudent_shouldReturnNoContent_whenIdExists() throws Exception {
		doNothing().when(studentService).deleteStudent(1L);

		mockMvc.perform(delete("/api/students/1")).andExpect(status().isNoContent());
	}

	@Test
	void deleteStudent_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
		doThrow(new StudentNotFoundException("Student not found with id: 5")).when(studentService).deleteStudent(5L);

		mockMvc.perform(delete("/api/students/5")).andExpect(status().isNotFound());
	}
}