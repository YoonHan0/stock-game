package com.stockgame.controller;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.domain.enums.UserRole;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userRepository, authService);
    }

    @Test
    void me_returnsUser_whenPrincipalIsEmail() {
        User user = User.builder()
                .id(9L)
                .email("me@example.com")
                .passwordHash("hashed")
                .nickname("me")
                .provider(AuthProvider.LOCAL)
                .role(UserRole.USER)
                .points(10L)
                .build();

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.me("me@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
