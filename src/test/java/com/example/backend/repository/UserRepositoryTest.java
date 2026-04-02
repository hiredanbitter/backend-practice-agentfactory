package com.example.backend.repository;

import com.example.backend.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_persistsUserAndAssignsId() {
        User user = new User("alice", "alice@example.com");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findById_returnsUserWhenExists() {
        User saved = userRepository.save(new User("bob", "bob@example.com"));

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob");
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        Optional<User> found = userRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllPersistedUsers() {
        userRepository.save(new User("charlie", "charlie@example.com"));
        userRepository.save(new User("diana", "diana@example.com"));

        List<User> users = userRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteById_removesUser() {
        User saved = userRepository.save(new User("eve", "eve@example.com"));

        userRepository.deleteById(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findByEmail_returnsUserWhenExists() {
        userRepository.save(new User("frank", "frank@example.com"));

        Optional<User> found = userRepository.findByEmail("frank@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("frank");
    }

    @Test
    void createdAt_isPopulatedByDatabase() {
        User saved = userRepository.save(new User("grace", "grace@example.com"));
        em.flush();
        em.refresh(saved);

        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
