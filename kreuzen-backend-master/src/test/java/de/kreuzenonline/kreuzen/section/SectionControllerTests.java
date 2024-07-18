package de.kreuzenonline.kreuzen.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.section.requests.CreateSectionRequest;
import de.kreuzenonline.kreuzen.section.requests.UpdateSectionRequest;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class SectionControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SectionRepo sectionRepo;

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSectionShouldCreateASection() throws Exception {
        String section = new ObjectMapper().writeValueAsString(new CreateSectionRequest("Bachelor"));
        when(sectionRepo.save(Mockito.any(Section.class))).thenReturn(new Section(1, 1, "Bachelor"));
        mvc.perform(post("/major/1/section").accept(MediaType.APPLICATION_JSON).content(section).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bachelor"))
                .andExpect(jsonPath("$.majorId").value("1"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSectionNotPossibleNoName() throws Exception {
        String section = new ObjectMapper().writeValueAsString(new CreateSectionRequest(""));
        mvc.perform(post("/major/1/section").accept(MediaType.APPLICATION_JSON).content(section).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSectionNotPossibleNotAdmin() throws Exception {
        String section = new ObjectMapper().writeValueAsString(new CreateSectionRequest(""));
        mvc.perform(post("/major/1/section").accept(MediaType.APPLICATION_JSON).content(section).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void sectionDoesNotExist() throws Exception {
        when(sectionRepo.findById(23))
                .thenReturn(java.util.Optional.empty());

        mvc.perform(get("/section/23")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getSectionShouldGetASection() throws Exception {
        when(sectionRepo.findById(1))
                .thenReturn(java.util.Optional.of(new Section(1, 1, "Bachelor")));

        mvc.perform(get("/section/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.majorId").value("1"))
                .andExpect(jsonPath("$.name").value("Bachelor"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void sectionsByMajor() throws Exception {
        when(sectionRepo.getAllSectionsByMajorId(1))
                .thenReturn(Collections.singletonList(new Section(1, 1, "Bachelor")));
        mvc.perform(get("/major/1/section")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Bachelor"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void sectionsByModule() throws Exception {
        when(sectionRepo.findAllByModuleId(1))
                .thenReturn(Collections.singletonList(new Section(1, 1, "Bachelor")));
        mvc.perform(get("/module/1/section")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Bachelor"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void patchBachelorToMaster() throws Exception {

        String section = new ObjectMapper().writeValueAsString(new UpdateSectionRequest("Master"));
        when(sectionRepo.findById(1))
                .thenReturn(java.util.Optional.of(new Section(1, 1, "Bachelor")));
        when(sectionRepo.save(Mockito.any(Section.class))).thenAnswer(s -> s.getArgument(0));

        mvc.perform(patch("/section/1").accept(MediaType.APPLICATION_JSON).content(section).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Master"))
                .andExpect(jsonPath("$.majorId").value("1"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateSectionNotAllowed() throws Exception {
        mvc.perform(patch("/section/1").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateSectionNotPossibleWithoutName() throws Exception {
        mvc.perform(patch("/section/1").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void sectionCanBeDeleted() throws Exception {
        doNothing().when(sectionRepo).deleteById(1);
        mvc.perform(delete("/section/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(sectionRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void sectionCanNotBeDeletedByUser() throws Exception {
        doNothing().when(sectionRepo).deleteById(1);
        mvc.perform(delete("/section/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
        verify(sectionRepo, times(0)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userHasBachelorAndMaster() throws Exception {
        List<Section> sections = new ArrayList<>();
        sections.add(new Section(1, 1, "Bachelor"));
        sections.add(new Section(2, 1, "Master"));
        when(sectionRepo.getSectionsByUserIdAndMajorId(45, 1))
                .thenReturn(sections);

        mvc.perform(get("/user/45/major/1/section")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Bachelor"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Master"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userHasNoSections() throws Exception {
        when(sectionRepo.getSectionsByUserIdAndMajorId(45, 1))
                .thenReturn(new ArrayList<>());
        mvc.perform(get("/user/45/major/1/section")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userAddsAnotherSectionToHisMajor() throws Exception {
        when(sectionRepo.findById(12))
                .thenReturn(java.util.Optional.of(new Section(12, 1, "Vorklinik")));

        mvc.perform(put("/user/45/major/1/section/12")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.majorId").value(1))
                .andExpect(jsonPath("$.name").value("Vorklinik"));
        verify(sectionRepo, times(1)).addUserToSection(45, 1, 12);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 42, role = "MOD")
    public void modCanNotAddAnotherSectionToUsersMajor() throws Exception {
        mvc.perform(put("/user/45/major/1/section/12"))
                .andExpect(status().is4xxClientError());
        verify(sectionRepo, times(0)).addUserToSection(45, 1, 12);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 12, role = "ADMIN")
    public void AdminCanRemoveSectionFromUsersMajor() throws Exception {
        doNothing().when(sectionRepo).removeUserFromSection(45, 1, 2);
        mvc.perform(delete("/user/45/major/1/section/2"))
                .andExpect(status().isNoContent());
        verify(sectionRepo, times(1)).removeUserFromSection(45, 1, 2);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 43)
    public void UserCanNotRemoveSectionFromAnotherUsersMajor() throws Exception {
        mvc.perform(delete("/user/45/major/1/section/2"))
                .andExpect(status().is4xxClientError());
        verify(sectionRepo, times(0)).removeUserFromSection(45, 1, 2);
    }

}
