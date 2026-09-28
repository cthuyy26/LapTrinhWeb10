package vn.iotstar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import vn.iotstar.services.JwtService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";
    private final long expirationTime = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expirationTime);
    }

    @Test
    void testGenerateAndValidateTokenWithNimbus() {
        UserDetails userDetails = new User("trungnh@hcmute.edu.vn", "password123", Collections.emptyList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("fullName", "ThS. Nguyen Huu Trung");

        String token = jwtService.generateToken(extraClaims, userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Extract username (subject)
        String username = jwtService.extractUsername(token);
        assertEquals("trungnh@hcmute.edu.vn", username);

        // Check token validity
        assertTrue(jwtService.isTokenValid(token, userDetails));

        // Check invalid user
        UserDetails otherUser = new User("other@example.com", "pass", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testTamperedTokenFailsVerification() {
        UserDetails userDetails = new User("admin@test.com", "pass", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // Tamper token payload by replacing characters
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        String tamperedToken = parts[0] + "." + parts[1] + "tampered" + "." + parts[2];

        assertThrows(BadCredentialsException.class, () -> {
            jwtService.extractAllClaims(tamperedToken);
        });
    }

    @Test
    void testExpiredTokenValidation() {
        // Set short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // already expired

        UserDetails userDetails = new User("expired@test.com", "pass", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenExpired(token));
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }
}
