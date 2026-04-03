package com.example.backend.repository;

import com.example.backend.domain.Post;
import com.example.backend.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PostRepository postRepository;

    @Test
    void findByUserId_returnsPostsForUser() {
        User user = em.persist(new User("testuser", "testuser@example.com"));
        em.persist(new Post("First Post", user));
        em.persist(new Post("Second Post", user));
        em.flush();

        List<Post> posts = postRepository.findByUserId(user.getId());

        assertThat(posts).hasSize(2);
        assertThat(posts).extracting(Post::getTitle)
                .containsExactlyInAnyOrder("First Post", "Second Post");
    }

    @Test
    void findByUserId_returnsEmptyListWhenNoPostsExist() {
        User user = em.persist(new User("emptyuser", "emptyuser@example.com"));
        em.flush();

        List<Post> posts = postRepository.findByUserId(user.getId());

        assertThat(posts).isEmpty();
    }

    @Test
    void findByUserId_doesNotReturnPostsFromOtherUsers() {
        User user1 = em.persist(new User("user1", "user1@example.com"));
        User user2 = em.persist(new User("user2", "user2@example.com"));
        em.persist(new Post("User1 Post", user1));
        em.persist(new Post("User2 Post", user2));
        em.flush();

        List<Post> posts = postRepository.findByUserId(user1.getId());

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("User1 Post");
    }
}
