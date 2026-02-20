package io.github.drompincen.archviz.controller;

import io.github.drompincen.archviz.dto.DiagramCreateRequest;
import io.github.drompincen.archviz.model.Diagram;
import io.github.drompincen.archviz.model.DiagramSummary;
import io.github.drompincen.archviz.service.DiagramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiagramApiController.class)
class DiagramApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiagramService diagramService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listDiagrams_returnsAll() throws Exception {
        List<DiagramSummary> summaries = List.of(
                new DiagramSummary("1", "Test", "Desc", List.of("tag1"), 1, "db"));
        when(diagramService.listAll(Optional.empty(), Optional.empty())).thenReturn(summaries);

        mockMvc.perform(get("/api/diagrams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("Test"));
    }

    @Test
    void listDiagrams_withTagFilter() throws Exception {
        when(diagramService.listAll(Optional.of("java"), Optional.empty()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/diagrams").param("tag", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDiagram_found() throws Exception {
        Diagram d = new Diagram();
        d.setId("1");
        d.setTitle("Test Diagram");
        d.setTags(List.of());
        d.setVersion(1);
        d.setSource("db");
        when(diagramService.getById("1")).thenReturn(Optional.of(d));

        mockMvc.perform(get("/api/diagrams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Test Diagram"));
    }

    @Test
    void getDiagram_notFound() throws Exception {
        when(diagramService.getById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/diagrams/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDiagram_returns201() throws Exception {
        Diagram created = new Diagram();
        created.setId("new-id");
        created.setTitle("New Diagram");
        created.setTags(List.of("tag1"));
        created.setVersion(1);
        created.setSource("db");
        created.setCreatedAt(Instant.now());
        created.setUpdatedAt(Instant.now());

        when(diagramService.create(any())).thenReturn(created);

        ObjectNode flow = objectMapper.createObjectNode();
        flow.put("title", "Test");
        DiagramCreateRequest request = new DiagramCreateRequest("New Diagram", "Desc", List.of("tag1"), flow);

        mockMvc.perform(post("/api/diagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.title").value("New Diagram"));
    }

    @Test
    void updateDiagram_found() throws Exception {
        Diagram updated = new Diagram();
        updated.setId("1");
        updated.setTitle("Updated");
        updated.setTags(List.of());
        updated.setVersion(2);
        updated.setSource("db");

        when(diagramService.update(eq("1"), any())).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/diagrams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"description\":null,\"tags\":[],\"flow\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void updateDiagram_notFound() throws Exception {
        when(diagramService.update(eq("missing"), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/diagrams/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"X\",\"description\":null,\"tags\":[],\"flow\":null}"))
                .andExpect(status().isNotFound());
    }
}
