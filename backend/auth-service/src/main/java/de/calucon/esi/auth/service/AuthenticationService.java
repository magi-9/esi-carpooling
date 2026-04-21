package de.calucon.esi.auth.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.calucon.esi.auth.dto.AuthenticationRequest;
import de.calucon.esi.auth.dto.AuthenticationResponse;
import de.calucon.esi.auth.dto.RegisterRequest;
import de.calucon.esi.auth.event.UserEventProducer;
import de.calucon.esi.auth.event.UserRegisteredEvent;
import de.calucon.esi.auth.model.Role;
import de.calucon.esi.auth.model.User;
import de.calucon.esi.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserEventProducer userEventProducer;

    public AuthenticationResponse register(RegisterRequest request) throws IllegalArgumentException {
        // 1. Check if user already exists (Optional but highly recommended)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // 2. Create the new User entity, hashing the password
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(request.getRoles())
                .build();

        // 3. Save to database
        userRepository.save(user);

        // 4. Broadcast Event to Kafka
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
        userEventProducer.publishUserRegisteredEvent(event);

        // 5. Generate token with extra claims (useful for your microservices!)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getRoles());
        extraClaims.put("userId", user.getId());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        // 5. Return the token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse login(AuthenticationRequest request) throws NoSuchElementException {
        // 1. Let Spring Security do the heavy lifting of checking the password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // 2. If we get here, the password is correct. Fetch the user.
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(); // Shouldn't happen if authentication passed

        // 3. Generate token with extra claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getRoles());
        extraClaims.put("userId", user.getId());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        // 4. Return the token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public Set<Role> getUserRoles() throws NoSuchElementException {
        var currentUser = getCurrentUser();

        var user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return user.getRoles();
    }

    public Set<Role> updateUserRoles(Set<Role> newRoles)
            throws NoSuchElementException {
        var currentUser = getCurrentUser();

        var user = userRepository
                .findById(currentUser.getId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + currentUser.getId()));

        user.setRoles(newRoles);
        userRepository.save(user); // Spring Data JPA automatically updates the user_roles table

        return user.getRoles();
    }

    public boolean hasRole(Role role) {
        return getCurrentAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }

    public AuthenticationResponse refreshToken() {
        var user = getCurrentUser();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getRoles());
        extraClaims.put("userId", user.getId());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private User getCurrentUser() {
        return (User) getCurrentAuthentication().getPrincipal();
    }

    private @Nullable Authentication getCurrentAuthentication() throws IllegalStateException {
        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        return authentication;
    }

}
