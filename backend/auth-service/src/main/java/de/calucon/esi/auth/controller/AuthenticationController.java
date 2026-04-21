package de.calucon.esi.auth.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.calucon.esi.auth.dto.AuthenticationRequest;
import de.calucon.esi.auth.dto.RegisterRequest;
import de.calucon.esi.auth.dto.RoleUpdateRequest;
import de.calucon.esi.auth.model.Role;
import de.calucon.esi.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and role management")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token. Roles can be DRIVER, PASSENGER, or both.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered user and returned JWT token"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or user already exists", content = @Content),
            @ApiResponse(responseCode = "503", description = "Registration temporarily unavailable", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.register(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (org.springframework.kafka.KafkaException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Registration temporarily unavailable");
        }
    }

    @Operation(summary = "Authenticate user", description = "Verifies user credentials and returns a valid JWT token for subsequent API calls.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated and returned JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.login(request));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @Operation(summary = "Logout user", description = "Clears the security context. Note: Since JWT is stateless, the client must also delete the token locally to complete the logout.")
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Because JWT is stateless, the server doesn't "store" sessions.
        // Real logout happens when the client deletes the token.
        // We clear the context here as a formality.
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }

    @Operation(summary = "Validate Token", description = "Used by other microservices. If this returns 200 OK, the token provided in the Authorization header is valid.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token is missing or expired", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        // MAGIC TRICK: We don't need any logic here!
        // Why? Because your JwtAuthenticationFilter intercepts this request first.
        // If the token is invalid or missing, the filter blocks it and returns 403
        // Forbidden.
        // If the code reaches this line, the token is 100% valid.
        return ResponseEntity.ok("Token is valid");
    }

    @Operation(summary = "Get User Roles", description = "Retrieves the Driver/Passenger roles for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/roles")
    public ResponseEntity<?> getUserRoles() {
        try {
            return ResponseEntity.ok(authenticationService.getUserRoles());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Update User Roles", description = "Overwrites the current roles for the authenticated user (e.g., upgrading a passenger to a driver).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PutMapping("/roles")
    public ResponseEntity<?> updateUserRoles(
            @Valid @RequestBody RoleUpdateRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.updateUserRoles(request.getRoles()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Check User Role", description = "Returns 200 OK if the current user has the specified role, or 403 Forbidden if they do not.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User has the specified role"),
            @ApiResponse(responseCode = "401", description = "Token is missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "User does not have the specified role", content = @Content)
    })
    @GetMapping("/validate/role/{role}")
    public ResponseEntity<Void> hasRole(@PathVariable Role role) {
        boolean hasRole = authenticationService.hasRole(role);
        if (hasRole) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).build();
        }
    }

}
