package com.app.AutomationSales.security.auth;

import com.app.AutomationSales.appUser.UserDto.UserResponseDto;
import com.app.AutomationSales.appUser.UserRepository.UserRepository;
import com.app.AutomationSales.appUser.model.User;
import com.app.AutomationSales.security.JwtService;
import com.app.AutomationSales.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /** Public. Wrong email/password → 401, disabled account → 401 (see GlobalExceptionHandler). */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));

        SecurityUser principal = (SecurityUser) authentication.getPrincipal();

        User user = userRepository.findById(principal.getUserId()).orElseThrow();
        user.setLogIn(Instant.now());

        return ResponseEntity.ok(new AuthResponse(
                jwtService.generateToken(principal),
                "Bearer",
                jwtService.getExpirationSeconds(),
                UserResponseDto.from(user)));
    }

    /** Who am I? Any valid token. */
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal SecurityUser me) {
        User user = userRepository.findById(me.getUserId()).orElseThrow();
        return ResponseEntity.ok(UserResponseDto.from(user));
    }
}
