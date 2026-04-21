package de.calucon.esi.auth.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import de.calucon.esi.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract the Authorization header from the incoming request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Check if the header is missing or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Not a JWT request. Pass it down the chain (might be a public endpoint like
            // /login)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the actual token (skip the "Bearer " prefix which is 7 characters)
        jwt = authHeader.substring(7);

        try {
            // 4. Extract the user email from the token
            userEmail = jwtService.extractUsername(jwt);

            // 5. If we have an email and the user is NOT already authenticated in this
            // session
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Fetch the user from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 6. If the token is valid, create an authentication token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No credentials (password) needed here because the JWT proves who they are
                            userDetails.getAuthorities() // Their roles (Driver/Passenger)
                    );

                    // Add extra details about the web request (IP address, session ID, etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7. Update the Security Context. This tells Spring: "This user is officially
                    // logged in!"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            // Invalid Token, treat as missing auth token
        }

        // 8. Continue the filter chain so the request can eventually reach your
        // Controllers
        filterChain.doFilter(request, response);
    }
}
