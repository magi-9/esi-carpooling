package de.calucon.esi.auth.event;

import java.util.Set;
import java.util.UUID;

import de.calucon.esi.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private UUID userId;
    private String email;
    private Set<Role> roles;
}
