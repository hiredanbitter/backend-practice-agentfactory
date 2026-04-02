package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User buildUser(Long id, String username, String email) {
        try {
            var constructor = User.class.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            User user = constructor.newInstance(username, email);
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createUser_returnsCreated() throws Exception {
        User saved = buildUser(1L, "alice", "alice@example.com");
        when(userService.createUser("alice", "alice@example.com")).thenReturn(saved);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"alice@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUserById_existing_returnsOk() throws Exception {
        User user = buildUser(1L, "alice", "alice@example.com");
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new NoSuchElementException("User not found with id: 99"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_returnsOkWithList() throws Exception {
        User u1 = buildUser(1L, "alice", "alice@example.com");
        User u2 = buildUser(2L, "bob", "bob@example.com");
        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].username").value("bob"));
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void createUser_blankUsername_returns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"alice@example.com\"}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString());
    }
}
