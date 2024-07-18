package de.kreuzenonline.kreuzen.role;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.responses.RoleResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@RestController
public class RoleController {

    private final RoleService roleService;
    private final ResourceBundle resourceBundle;

    public RoleController(RoleService roleService, ResourceBundle resourceBundle) {
        this.roleService = roleService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/role")
    @ApiOperation(
            value = "Get list of all available roles."
    )
    public List<RoleResponse> getAvailableRoles(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        List<Role> roles = roleService.getAllRoles();

        return roles.stream().map(RoleResponse::new).collect(Collectors.toList());
    }
}
