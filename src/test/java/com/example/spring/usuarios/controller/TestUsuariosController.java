package com.example.spring.usuarios.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.spring.usuarios.controller.error.UsuarioNotFoundException;
import com.example.spring.usuarios.controller.error.UsuarioRepetidoException;
import com.example.spring.usuarios.model.Usuario;
import com.example.spring.usuarios.service.UsuarioService;
import org.springframework.http.MediaType;

import com.example.spring.usuarios.controller.error.InvalidPasswordException;
import com.example.spring.usuarios.controller.error.ListEmptyException;
import static org.hamcrest.Matchers.lessThan;

@SpringBootTest
@AutoConfigureMockMvc
public class TestUsuariosController {

	@MockBean
	private UsuarioService servicio;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testFindByIdParametroIgual() throws Exception {

		Long id = (long) 8;
		Usuario usuarioEsperado = new Usuario(id, "prueba", "prueba", "prueba@gmail.com", "prueba", LocalDate.now());

		when(servicio.findById(id)).thenReturn(usuarioEsperado);

		mockMvc.perform(get("/usuario/{id}", id)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));

	}

	@Test
	void testFindByIdUsuarioNoExiste() throws Exception {
		Long id = (long) 1;

		when(servicio.findById(id)).thenThrow(new UsuarioNotFoundException(id));

		mockMvc.perform(get("/usuario/{id}", id)).andDo(print()).andExpect(status().isNotFound());

	}

	@Test
	void testDeleteByIdUsuarioNoExiste() throws Exception {
		Long id = (long) 1;

		when(servicio.findById(id)).thenThrow(new UsuarioNotFoundException(id));

		mockMvc.perform(delete("/usuario/{id}", id)).andDo(print()).andExpect(status().isNotFound());

	}

	@Test
	public void testDeleteByIdUsuarioEliminadoCorrectamente() throws Exception {

		Long id = (long) 8;
		Usuario usuario = new Usuario(id, "prueba", "prueba", "prueba@gmail.com", "prueba", LocalDate.now());
		when(servicio.findById(id)).thenReturn(usuario);

		mockMvc.perform(delete("/usuario/{id}", id)).andDo(print()).andExpect(status().isAccepted());

		verify(servicio, times(1)).deleteById(id);
	}

	public void testAltaUsuarioExitoso() throws Exception {
        when(servicio.save(any())).thenReturn(new Usuario());

        String usuarioJson = "{\r\n"
        		+ "    \"nombre\": \"nuevoUsuario\",\r\n"
        		+ "    \"apellido\": \"nuevoUsuario\",\r\n"
        		+ "    \"mail\": \"nuevoUsuario@gmail.com\",\r\n"
        		+ "    \"contrasenia\": \"1234\"\r\n"
        		+ "}";
        
        mockMvc.perform(post("/usuario")
                .content(usuarioJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

	@Test
	void testAltaUsuarioRepetido() throws Exception {
		String errorMessage = "Epic Fail: Ya existe un usuario con ese correo electronico";

		when(servicio.save(any())).thenThrow(new UsuarioRepetidoException());

		String usuarioJson = "{\r\n" + "    \"nombre\": \"pruebaUsuario\",\r\n" + "    \"apellido\": \"Usuario\",\r\n"
				+ "    \"mail\": \"usuario@gmail.com\",\r\n" + "    \"contrasenia\": \"1234\",\r\n"
				+ "    \"fechaAlta\": \"02-12-2023\"\r\n" + "}";

		mockMvc.perform(post("/usuario").content(usuarioJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value(errorMessage));
	}

	@Test
	void testEditarInvalidPassword() throws Exception {
		String errorMessage = "Epic Fail: La contraseña debe tener al menos 4 caracteres";

		Usuario usuarioExistente = new Usuario();
		usuarioExistente.setIdUsuario(9L);

		when(servicio.findById(9L)).thenReturn(usuarioExistente);
		when(servicio.save(any())).thenThrow(new InvalidPasswordException());

		String usuarioJson = "{\r\n" + "    \"nombre\": \"pruebaUsuario\",\r\n" + "    \"apellido\": \"Usuario\",\r\n"
				+ "    \"mail\": \"usuarioExcepcion@gmail.com\",\r\n" + "    \"contrasenia\": \"123\",\r\n"
				+ "    \"fechaAlta\": \"02-12-2023\"\r\n" + "}";

		mockMvc.perform(put("/usuario/{id}", 9).content(usuarioJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(errorMessage));
	}

	@Test
	void testEditarUsuarioExitoso() throws Exception {
		Usuario usuarioExistente = new Usuario();
		usuarioExistente.setIdUsuario(9L);

		when(servicio.findById(9L)).thenReturn(usuarioExistente);
		when(servicio.save(any())).thenReturn(usuarioExistente);

		String usuarioJson = "{\r\n" + "    \"nombre\": \"pruebaUsuario\",\r\n" + "    \"apellido\": \"Usuario\",\r\n"
				+ "    \"mail\": \"usuarioNuevooo@gmail.com\",\r\n" + "    \"contrasenia\": \"1234\",\r\n"
				+ "    \"fechaAlta\": \"02-12-2023\"\r\n" + "}";

		mockMvc.perform(put("/usuario/{id}", 9).content(usuarioJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	void testEditarUsuarioRepetido() throws Exception {
		String errorMessage = "Epic Fail: Ya existe un usuario con ese correo electronico";

		Usuario usuarioExistente = new Usuario();
		usuarioExistente.setIdUsuario(8L);

		when(servicio.findById(8L)).thenReturn(usuarioExistente);
		when(servicio.save(any())).thenThrow(new UsuarioRepetidoException());

		String usuarioJson = "{\r\n" + "    \"nombre\": \"pruebaUsuario\",\r\n" + "    \"apellido\": \"Usuario\",\r\n"
				+ "    \"mail\": \"usuario@gmail.com\",\r\n" + "    \"contrasenia\": \"1234\",\r\n"
				+ "    \"fechaAlta\": \"02-12-2023\"\r\n" + "}";

		mockMvc.perform(put("/usuario/{id}", 8).content(usuarioJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value(errorMessage));
	}

	@Test
	void testFindAllListaVacia() throws Exception{
		
		when(servicio.findAll()).thenThrow(new ListEmptyException());
		
		mockMvc.perform(get("/usuario")).andDo(print()).andExpect(status().isNotFound());
		
	}
	
	@Test
	void testFindAllFechaAltaAnteriorDiaActual() throws Exception{
		
		List<Usuario> usuarios = new ArrayList<Usuario>();
		usuarios.add(new Usuario("usuario1","usuario1","usuario1@gmail.com","usuario1",LocalDate.now().minusDays(1)));
		usuarios.add(new Usuario("usuario2","usuario2","usuario2@gmail.com","usuario2",LocalDate.now().minusDays(2)));
		usuarios.add(new Usuario("usuario3","usuario3","usuario3@gmail.com","usuario3",LocalDate.now().minusDays(3)));
		
		
		when(servicio.findAll()).thenReturn(usuarios);
		
		mockMvc.perform(get("/usuario")).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$[*].fechaAlta", everyItem(lessThan(LocalDate.now().toString()))));
		
	}
	
	@Test
	void testFindAllUsuarioConApellidoGarcia() throws Exception{
		
		List<Usuario> usuarios = new ArrayList<Usuario>();
		usuarios.add(new Usuario("usuario1","Garcia","usuario1@gmail.com","usuario1",LocalDate.now()));
		usuarios.add(new Usuario("usuario2","usuario2","usuario2@gmail.com","usuario2",LocalDate.now()));
		usuarios.add(new Usuario("usuario3","usuario3","usuario3@gmail.com","usuario3",LocalDate.now()));
		
		
		when(servicio.findAll()).thenReturn(usuarios);
		
		mockMvc.perform(get("/usuario")).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$[*].apellido", hasItem(equalTo("Garcia"))));
		
	}
	
}
