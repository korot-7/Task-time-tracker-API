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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedUsername = request.username().trim();
        if (userMapper.existsByUsername(normalizedUsername)) {
            throw new BadRequestException("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        userMapper.insert(user);

        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);
        return new AuthResponse(token, "Bearer", jwtTokenProvider.getExpirationSeconds());
    }
}
