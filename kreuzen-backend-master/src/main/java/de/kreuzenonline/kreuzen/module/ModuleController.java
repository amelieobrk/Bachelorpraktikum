package de.kreuzenonline.kreuzen.module;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.module.requests.CreateModuleRequest;
import de.kreuzenonline.kreuzen.module.requests.DeleteModuleRequest;
import de.kreuzenonline.kreuzen.module.requests.UpdateModuleRequest;
import de.kreuzenonline.kreuzen.module.responses.ModuleResponse;
import de.kreuzenonline.kreuzen.role.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@RestController
@Api(tags = "Module")
public class ModuleController {

    private final ModuleService moduleService;
    private final ResourceBundle resourceBundle;
    private final PasswordEncoder passwordEncoder;

    public ModuleController(ModuleService moduleService, ResourceBundle resourceBundle, PasswordEncoder passwordEncoder) {
        this.moduleService = moduleService;
        this.resourceBundle = resourceBundle;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/module/{id}")
    @ApiOperation(
            value = "Get module",
            notes = "Get a specific module by its id."
    )
    public ModuleResponse getModule(@PathVariable Integer id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-module-forbidden"));
        }

        Module module = moduleService.getById(id);

        return new ModuleResponse(module);
    }


    @PostMapping("/module")
    @ApiOperation(
            value = "Create module",
            notes = "Create a new module."
    )
    public ModuleResponse createModule(@Valid @RequestBody CreateModuleRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-module-forbidden"));
        }

        Module module = moduleService.create(request.getName(), request.getUniversityId(), request.getIsUniversityWide());

        return new ModuleResponse(module);
    }


    @PatchMapping("module/{id}")
    @ApiOperation(
            value = "Update module",
            notes = "Update the information of a module. All values are optional. If a value is set, then it is updated."
    )
    public ModuleResponse updateModule(@PathVariable Integer id, @Valid @RequestBody UpdateModuleRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-module-forbidden"));
        }

        Module module = moduleService.update(id, request.getName(), request.getUniversityId(), request.getIsUniversityWide());

        return new ModuleResponse(module);
    }

    @DeleteMapping("module/{id}")
    @ApiOperation(
            value = "Delete module",
            notes = "Deletes a specific module by its id"
    )
    public ResponseEntity<Void> deleteModule(@PathVariable Integer id, @Valid @RequestBody DeleteModuleRequest request,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        request.setId(id);
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-module-forbidden"));
        }


        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new ForbiddenException(resourceBundle.getString("delete-module-password-wrong"));
        }

        moduleService.delete(request.getId());

        return ResponseEntity.noContent().build();

    }


    @GetMapping("/module")
    @ApiOperation(value = "Get all modules")
    public List<ModuleResponse> getAllModules(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-all-modules-forbidden"));
        }

        Iterable<Module> modules = moduleService.getAll();
        List<ModuleResponse> responses = new ArrayList<>();
        for (Module m : modules) {
            responses.add(new ModuleResponse(m));
        }
        return responses;
    }

    @GetMapping("/university/{uniId}/module")
    @ApiOperation(
            value = "Get modules by university"

    )
    private List<ModuleResponse> getModulesByUniversity(@PathVariable Integer uniId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Iterable<Module> modules = moduleService.getModulesByUniversity(uniId);
        List<ModuleResponse> response = new ArrayList<>();
        for (Module module : modules) {
            response.add(new ModuleResponse(module));
        }

        return response;
    }

    @GetMapping("/user/{userId}/module")
    @ApiOperation(
            value = "Get modules by user"

    )
    private List<ModuleResponse> getModulesByUser(@PathVariable Integer userId, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only view their own modules. Admins and Sudo can edit all users' modules.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-user-modules-forbidden"));
        }
        List<Module> modules = moduleService.getModulesByUser(userId);
        List<ModuleResponse> responseList = new ArrayList<>();

        for (Module module : modules) {
            responseList.add(new ModuleResponse(module));
        }

        return responseList;
    }

    @PutMapping("/major/{majorId}/module/{moduleId}")
    @ApiOperation(
            value = "Add a module to the selected major"
    )
    private ModuleResponse addModuleToMajor(@PathVariable Integer majorId, @PathVariable Integer moduleId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("add-module-to-major-forbidden"));
        }

        moduleService.addModuleToMajor(moduleId, majorId);
        return new ModuleResponse(moduleService.getById(moduleId));
    }

    @DeleteMapping("/major/{majorId}/module/{moduleId}")
    @ApiOperation(
            value = "Removes a module from a major"

    )
    private ResponseEntity<Void> removeModuleFromMajor(@PathVariable Integer majorId, @PathVariable Integer moduleId,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-module-from-major-forbidden"));
        }

        moduleService.removeModuleFromMajor(moduleId, majorId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/section/{sectionId}/module/{moduleId}")
    @ApiOperation(
            value = "Add a module to the selected section"
    )
    private ModuleResponse addModuleToSection(@PathVariable Integer sectionId, @PathVariable Integer moduleId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("add-module-to-section-forbidden"));
        }

        moduleService.addModuleToSection(moduleId, sectionId);
        return new ModuleResponse(moduleService.getById(moduleId));
    }

    @DeleteMapping("/section/{sectionId}/module/{moduleId}")
    @ApiOperation(
            value = "Removes a module from a section"

    )
    private ResponseEntity<Void> removeModuleFromSection(@PathVariable Integer sectionId, @PathVariable Integer moduleId,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-module-from-section-forbidden"));
        }

        moduleService.removeModuleFromSection(moduleId, sectionId);
        return ResponseEntity.noContent().build();
    }


}


