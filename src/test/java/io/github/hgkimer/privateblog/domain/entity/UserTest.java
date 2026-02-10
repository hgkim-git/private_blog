package io.github.hgkimer.privateblog.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.hgkimer.privateblog.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void builder() {
        User user = User.builder().id(1L).email("test@example.com").password("password")
            .role(UserRole.VISITOR).build();
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(UserRole.VISITOR, user.getRole());
    }
}