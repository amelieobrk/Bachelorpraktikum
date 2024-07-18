package de.kreuzenonline.kreuzen.role;

import java.util.List;

public interface RoleService {

    /**
     * Get a list of all available roles.
     *
     * @return List of all roles
     */
    List<Role> getAllRoles();
}
