
package com.otms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.otms.service.AuditService;
import com.otms.service.LoginSecurityService;
import com.otms.config.JwtService;
import com.otms.dto.LoginRequest;
import com.otms.dto.LoginResponse;
import com.otms.model.UserAccount;
import com.otms.repository.UserAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuditService auditService;
    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final LoginSecurityService loginSecurityService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            JwtService jwtService,
            AuditService auditService,
            LoginSecurityService loginSecurityService) {

        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.loginSecurityService = loginSecurityService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        loginSecurityService.checkRateLimit(httpRequest.getRemoteAddr());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            loginSecurityService.recordFailure(request.email());

            auditService.log(
                    request.email(),
                    "LOGIN_FAILURE",
                    "AUTH",
                    null,
                    "Failed login attempt"
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        loginSecurityService.recordSuccess(user.getEmail());

        auditService.log(
                user.getEmail(),
                "LOGIN_SUCCESS",
                "AUTH",
                user.getId(),
                "User logged in successfully"
        );

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        String refreshToken = jwtService.generateRefreshToken(
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        return new LoginResponse(
                token, refreshToken, user.getName(), user.getRole()
        );
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestParam String refreshToken) {

        var claims = jwtService.parseRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid or expired refresh token"
                ));

        UserAccount user = userAccountRepository.findByEmail(claims.getSubject())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Account unavailable"
                ));

        String newAccessToken = jwtService.generateToken(
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                user.getName(),
                user.getRole()
        );
    }

    @PostMapping("/register")
    public Map<String, String> registerPlaceholder() {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Self-registration is disabled. Ask an administrator to create accounts."
        );
    }
}
