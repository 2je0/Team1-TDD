package com.tdd.backend.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.backend.user.data.UserCreate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	UserController userController;
	@Autowired
	UserRepository userRepository;

	@Test
	@DisplayName("유저 회원가입")
	void signup() throws Exception {
		//given
		UserCreate userCreate = UserCreate.builder()
			.email("test@tdd.com")
			.userPassword("pass")
			.userName("tester")
			.phoneNumber("0101010")
			.build();

		String jsonRequest = objectMapper.writeValueAsString(userCreate);

		//when
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isFound())
			.andDo(print());

		//then
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(userRepository.count()).isEqualTo(1);

		User user = userRepository.findByEmail(userCreate.getEmail())
			.orElseThrow(RuntimeException::new);
		softAssertions.assertThat(user.getUserName()).isEqualTo(userCreate.getUserName());

		softAssertions.assertAll();
	}

	@Test
	@DisplayName("유저 회원가입_null 값 들어옴.")
	void signup_null() throws Exception {
		//given
		UserCreate userCreate = UserCreate.builder()
			.email(null)
			.userPassword("pass")
			.userName("tester")
			.phoneNumber("0101010")
			.build();

		String jsonRequest = objectMapper.writeValueAsString(userCreate);

		//expected
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.toString()))
			.andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.validation.email").value("email 값은 필수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("유저 회원가입_중복 이메일 들어옴.")
	void signup_duplicate_email() throws Exception {
		//given
		userRepository.save(new User("test@tdd.com", "tester", "0101010", "pass"));

		//when
		mockMvc.perform(get("/users/validation/{email}}", "test@tdd.com")
				.contentType(MediaType.TEXT_HTML))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.toString()))
			.andExpect(jsonPath("$.errorMessage").value("중복된 이메일입니다."))
			.andDo(print());
	}
}
