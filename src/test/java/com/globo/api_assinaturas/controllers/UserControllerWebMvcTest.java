package com.globo.api_assinaturas.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.globo.api_assinaturas.controller.UserController;
import com.globo.api_assinaturas.dto.CreateUserRequest;
import com.globo.api_assinaturas.dto.UserResponse;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.handler.GlobalExceptionHandler;
import com.globo.api_assinaturas.service.UserService;

@WebMvcTest(controllers = { UserController.class, GlobalExceptionHandler.class })
class UserControllerWebMvcTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private UserService service;

	@Test
	void create_returns201() throws Exception {
		UUID id = UUID.randomUUID();

		Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

		when(service.create(any(CreateUserRequest.class)))
				.thenReturn(new UserResponse(id, "Joao", "joao@example.com", createdAt));

		mvc.perform(
				post("/users").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("""
						{
						  "name": "Joao",
						  "email": "joao@example.com"
						}
						""")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.name").value("Joao")).andExpect(jsonPath("$.email").value("joao@example.com"));
	}

	@Test
	void create_whenInvalid_returns400ProblemDetail() throws Exception {
		mvc.perform(
				post("/users").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("""
						{
						  "name": "",
						  "email": "email-invalido"
						}
						""")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Validation error"))
				.andExpect(jsonPath("$.instance").value("/users")).andExpect(jsonPath("$.errors").exists());
	}

	@Test
	void get_returns200() throws Exception {
		UUID id = UUID.randomUUID();

		Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

		when(service.get(id)).thenReturn(new UserResponse(id, "Joao", "joao@example.com", createdAt));

		mvc.perform(get("/users/{id}", id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString())).andExpect(jsonPath("$.name").value("Joao"));
	}

	@Test
	void get_whenNotFound_returns404ProblemDetail() throws Exception {
		UUID id = UUID.randomUUID();

		when(service.get(eq(id))).thenThrow(new NotFoundException("Usuário não encontrado"));

		mvc.perform(get("/users/{id}", id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Usuário não encontrado"))
				.andExpect(jsonPath("$.instance").value("/users/" + id));
	}
}