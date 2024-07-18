package de.kreuzenonline.kreuzen.module;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.module.requests.CreateModuleRequest;
import de.kreuzenonline.kreuzen.module.requests.DeleteModuleRequest;
import de.kreuzenonline.kreuzen.module.requests.UpdateModuleRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class ModuleControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ModuleRepo moduleRepo;


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createModule() throws Exception {
        String mod = new ObjectMapper().writeValueAsString(new CreateModuleRequest("Anatomy", 1, true));
        when(moduleRepo.save(Mockito.any(Module.class))).thenReturn(new Module(3, "Anatomy", 1, true));
        mvc.perform(post("/module").accept(MediaType.APPLICATION_JSON).content(mod).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Anatomy"))
                .andExpect(jsonPath("$.universityId").value(1))
                .andExpect(jsonPath("$.universityWide").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createModuleNotAdmin() throws Exception {
        String mod = new ObjectMapper().writeValueAsString(new CreateModuleRequest("Anatomy", 1, true));
        mvc.perform(post("/module")
                .accept(MediaType.APPLICATION_JSON)
                .content(mod)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));

    }


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateModule() throws Exception {
        String module = new ObjectMapper().writeValueAsString(new UpdateModuleRequest("NewModule", null, true));
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "OldModule", 1, false)));
        when(moduleRepo.save(Mockito.any(Module.class))).thenAnswer(m -> m.getArgument(0));
        mvc.perform(patch("/module/1").accept(MediaType.APPLICATION_JSON).content(module).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("NewModule"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.universityId").value(1))
                .andExpect(jsonPath("$.universityWide").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getModule() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Module1", 1, false)));
        mvc.perform(get("/module/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Module1"))
                .andExpect(jsonPath("$.universityId").value("1"))
                .andExpect(jsonPath("$.universityWide").value("false"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getModuleNotAdmin() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Module1", 1, false)));
        mvc.perform(get("/module/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteModule() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteModuleRequest(null, "p"));
        mvc.perform(delete("/module/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is(204));
        verify(moduleRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteModuleNotAdmin() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Mod1", 1, false)));
        mvc.perform(delete("/module/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN", password = "test1")
    public void deleteModuleWrongPassword() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteModuleRequest(null, "test2"));
        mvc.perform(delete("/module/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getAllModules() throws Exception {
        when(moduleRepo.findAll()).thenReturn(Collections.singletonList(new Module(1, "Mod1", 1, true)));
        mvc.perform(get("/module").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Mod1"))
                .andExpect(jsonPath("$[*].universityId").value(1))
                .andExpect(jsonPath("$[*].universityWide").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getAllModulesNotAdmin() throws Exception {
        when(moduleRepo.findAll()).thenReturn(Collections.singletonList(new Module(1, "Mod1", 1, true)));
        mvc.perform(get("/module").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getModulesByUniversityId() throws Exception {

        when(moduleRepo.findAllByUniversityId(1)).thenReturn(Collections.singletonList(new Module(1, "Mod1", 1, true)));
        mvc.perform(get("/university/1/module").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Mod1"))
                .andExpect(jsonPath("$[*].universityId").value(1))
                .andExpect(jsonPath("$[*].universityWide").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 1)
    public void getModulesByUser() throws Exception {
        when(moduleRepo.findAllByUserId(1)).thenReturn(Collections.singletonList(new Module(1, "Mod1", 1, true)));
        mvc.perform(get("/user/1/module").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Mod1"))
                .andExpect(jsonPath("$[*].universityId").value(1))
                .andExpect(jsonPath("$[*].universityWide").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void addModuleToMajor() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Mod1", 1, true)));
        doNothing().when(moduleRepo).addModuleToMajor(1,1);
        mvc.perform(put("/major/1/module/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mod1"))
                .andExpect(jsonPath("$.universityId").value(1))
                .andExpect(jsonPath("$.universityWide").value(true));

        verify(moduleRepo, times(1)).addModuleToMajor(1, 1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void addModuleToMajorNotAdmin() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Mod1", 1, true)));
        doNothing().when(moduleRepo).addModuleToMajor(1,1);
        mvc.perform(put("/major/1/module/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void removeModuleFromMajor() throws Exception {
        doNothing().when(moduleRepo).removeModuleFromMajor(1,1);
        mvc.perform(delete("/major/1/module/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(moduleRepo, times(1)).removeModuleFromMajor(1,1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void addModuleToSection() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Mod1", 1, true)));
        doNothing().when(moduleRepo).addModuleToMajorSection(1,1);
        mvc.perform(put("/section/1/module/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mod1"))
                .andExpect(jsonPath("$.universityId").value(1))
                .andExpect(jsonPath("$.universityWide").value(true));

        verify(moduleRepo, times(1)).addModuleToMajorSection(1, 1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void addModuleToSectionNotAdmin() throws Exception {
        when(moduleRepo.findById(1)).thenReturn(java.util.Optional.of(new Module(1, "Mod1", 1, true)));
        doNothing().when(moduleRepo).addModuleToMajorSection(1,1);
        mvc.perform(put("/section/1/module/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void removeModuleFromSection() throws Exception {
        doNothing().when(moduleRepo).removeModuleFromMajorSection(1,1);
        mvc.perform(delete("/section/1/module/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(moduleRepo, times(1)).removeModuleFromMajorSection(1,1);
    }



}
