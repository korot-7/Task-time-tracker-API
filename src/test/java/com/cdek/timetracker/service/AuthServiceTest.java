package com.cdek.timetracker.service;

import com.cdek.timetracker.dto.auth.AuthResponse;
import com.cdek.timetracker.dto.auth.LoginRequest;
import com.cdek.timetracker.dto.auth.RegisterRequest;
import com.cdek.timetracker.dto.auth.UserResponse;
import com.cdek.timetracker.exception.BadRequestException;
import com.cdek.timetracker.mapper.UserMapper;
import com.cdek.timetracker.model.AppUser;
import com.cdek.timetracker.model.UserRole;
import com.cdek.timetracker.security.JwtTokenProvider;
import com.cdek.timetracker.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldThrowWhenUsernameAlreadyExists() {
        when(userMapper.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("existing", "strongpass")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void registerShouldPersistUserAndReturnResponse() {
        when(userMapper.existsByUsername("new-user")).thenReturn(false);
        when(passwordEncoder.encode("strongpass")).thenReturn("hashed");
        doAnswer(invocation -> {
            AppUser appUser = invocation.getArgument(0);
            appUser.setId(7L);
            return null;
        }).when(userMapper).insert(any(AppUser.class));

        UserResponse response = authService.register(new RegisterRequest("new-user", "strongpass"));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.USER);
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(response.id()).isEqualTo(7L);
    }

    @Test
    void loginShouldReturnTokenPayload() {
        UserPrincipal principal = new UserPrincipal(2L, "alice", "hash", UserRole.USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(principal)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("alice", "secret"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
