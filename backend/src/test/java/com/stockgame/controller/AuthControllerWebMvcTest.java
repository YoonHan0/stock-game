package com.stockgame.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.domain.enums.UserRole;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.LoginRequestDto;
import com.stockgame.dto.SignupRequestDto;
import com.stockgame.security.JwtAuthenticationFilter;
import com.stockgame.security.OAuth2SuccessHandler;
import com.stockgame.security.oauth2.CustomOAuth2UserService;
import com.stockgame.service.AuthBadRequestException;
import com.stockgame.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void signup_returns201AndUser() throws Exception {
        SignupRequestDto request = new SignupRequestDto("new@example.com", "pw", "nick");
        User created = localUser(1L, "new@example.com", "nick");

        when(authService.signup(any(SignupRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void login_setsCookieAndReturnsUser() throws Exception {
        LoginRequestDto request = new LoginRequestDto("user@example.com", "pw");
        User user = localUser(2L, "user@example.com", "nick");
        ResponseCookie cookie = ResponseCookie.from("accessToken", "jwt-value")
                .httpOnly(true)
                .path("/")
                .build();

        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(new AuthService.LoginResult(user, cookie));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", cookie.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void logout_setsExpiredCookie() throws Exception {
        ResponseCookie expired = ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .build();
        when(authService.logoutCookie()).thenReturn(expired);

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("accessToken=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));
    }

    @Test
    void me_returnsUnauthorized_whenNoPrincipal() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void signup_returns400_whenServiceThrowsBadRequest() throws Exception {
        SignupRequestDto request = new SignupRequestDto("dup@example.com", "pw", "nick");
        when(authService.signup(any(SignupRequestDto.class)))
                .thenThrow(new AuthBadRequestException("이미 사용 중인 이메일입니다."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    private User localUser(Long id, String email, String nickname) {
        return User.builder()
                .id(id)
                .email(email)
                .passwordHash("hashed")
                .nickname(nickname)
                .provider(AuthProvider.LOCAL)
                .role(UserRole.USER)
                .points(0L)
                .build();
    }
}
