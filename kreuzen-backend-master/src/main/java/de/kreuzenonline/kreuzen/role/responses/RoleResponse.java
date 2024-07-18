package de.kreuzenonline.kreuzen.role.responses;

import de.kreuzenonline.kreuzen.role.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {

    private String name;
    private String displayName;

    public RoleResponse(Role role) {
        this.name = role.getName();
        this.displayName = role.getDisplayName();
    }
}
