package com.example.backend.service;

import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_savesAndReturnsUser() {
        User user = new User("alice", "alice@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("alice", "alice@example.com");

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_returnsUserWhenFound() {
        User user = new User("bob", "bob@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result.getUsername()).isEqualTo("bob");
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        List<User> users = List.of(new User("alice", "alice@example.com"), new User("bob", "bob@example.com"));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser_delegatesToRepository() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
