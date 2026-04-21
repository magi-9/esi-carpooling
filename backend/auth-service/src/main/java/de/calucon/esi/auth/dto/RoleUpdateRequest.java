package de.calucon.esi.auth.dto;

import java.util.Set;

import de.calucon.esi.auth.model.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateRequest {
    @NotEmpty(message = "At least one role must be provided")
    private Set<Role> roles;
}
