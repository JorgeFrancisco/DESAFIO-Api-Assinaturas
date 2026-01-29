package com.globo.api_assinaturas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.globo.api_assinaturas.domain.AppUser;
import com.globo.api_assinaturas.dto.CreateUserRequest;
import com.globo.api_assinaturas.dto.UserResponse;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private AppUserRepository repo;

	@InjectMocks
	private UserService service;

	@Test
	void create_normalizesEmail_andReturnsResponse() {
		CreateUserRequest req = new CreateUserRequest("Joao", "  joao@Example.Com  ");

		when(repo.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0, AppUser.class));

		UserResponse out = service.create(req);

		assertNotNull(out.id());
		assertEquals("Joao", out.name());
		assertEquals("joao@example.com", out.email());

		ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);

		verify(repo).save(captor.capture());

		AppUser saved = captor.getValue();

		assertEquals("Joao", saved.getName());
		assertEquals("joao@example.com", saved.getEmail());
		assertNotNull(saved.getId());
	}

	@Test
	void create_whenDuplicateEmail_throwsConflict() {
		CreateUserRequest req = new CreateUserRequest("Joao", "joao@example.com");

		when(repo.save(any(AppUser.class))).thenThrow(new DataIntegrityViolationException("dup"));

		ConflictException ex = assertThrows(ConflictException.class, () -> service.create(req));

		assertTrue(ex.getMessage().contains("Email já cadastrado"));

		verify(repo).save(any(AppUser.class));
	}

	@Test
	void getEntity_whenNotFound_throwsNotFound() {
		UUID id = UUID.randomUUID();

		when(repo.findById(id)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getEntity(id));

		assertEquals("Usuário não encontrado", ex.getMessage());

		verify(repo).findById(id);
	}

	@Test
	void get_returnsUserResponse() {
		UUID id = UUID.randomUUID();
		AppUser u = new AppUser(id, "Joao", "joao@example.com");

		when(repo.findById(id)).thenReturn(Optional.of(u));

		UserResponse out = service.get(id);

		assertEquals(id, out.id());
		assertEquals("Joao", out.name());
		assertEquals("joao@example.com", out.email());

		verify(repo).findById(id);
	}
}