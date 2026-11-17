package com.otms.service;

import com.otms.model.UserAccount;
import com.otms.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginSecurityService {

    private static final int MAX_FAILURES = 5;
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final UserAccountRepository users;
    private final ConcurrentHashMap<String, Window> requests = new ConcurrentHashMap<>();

    public LoginSecurityService(UserAccountRepository users) {
        this.users = users;
    }

    public void checkRateLimit(String ip) {
        long now = System.currentTimeMillis();

        Window window = requests.compute(ip, (key, old) -> {
            if (old == null || now - old.startedAt > WINDOW_SECONDS * 1000) {
                return new Window(now, 1);
            }
            old.count++;
            return old;
        });

        if (window.count > MAX_REQUESTS) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Try again later."
            );
        }
    }

    @Transactional
    public void recordFailure(String email) {
        users.findByEmail(email).ifPresent(user -> {
            if (user.isLocked()) {
                return;
            }

            int failures = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failures);

            if (failures >= MAX_FAILURES) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }

            users.save(user);
        });
    }

    @Transactional
    public void recordSuccess(String email) {
        users.findByEmail(email).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            users.save(user);
        });
    }

    private static class Window {
        final long startedAt;
        int count;

        Window(long startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
