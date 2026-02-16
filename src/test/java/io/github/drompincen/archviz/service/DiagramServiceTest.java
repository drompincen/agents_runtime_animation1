package io.github.drompincen.archviz.service;

import io.github.drompincen.archviz.dto.DiagramCreateRequest;
import io.github.drompincen.archviz.dto.DiagramUpdateRequest;
import io.github.drompincen.archviz.model.Diagram;
import io.github.drompincen.archviz.model.DiagramSummary;
import io.github.drompincen.archviz.repository.DiagramRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagramServiceTest {

    @Mock
    private DiagramRepository repository;

    @Mock
    private StaticFileService staticFileService;

    private DiagramService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new DiagramService(repository, staticFileService);
    }

    @Test
    void listAll_mergesDbAndStaticDiagrams() {
        Diagram dbDiagram = makeDiagram("db-1", "DB Diagram", "db");
        Diagram fileDiagram = makeDiagram("file-test", "File Diagram", "file");

        when(repository.findAll(Optional.empty(), Optional.empty()))
                .thenReturn(List.of(dbDiagram));
        when(staticFileService.loadStaticDiagrams())
                .thenReturn(List.of(fileDiagram));

        List<DiagramSummary> result = service.listAll(Optional.empty(), Optional.empty());
        assertEquals(2, result.size());
    }

    @Test
    void listAll_filtersStaticByTag() {
        Diagram fileDiagram = makeDiagram("file-test", "File Diagram", "file");
        fileDiagram.setTags(List.of("java"));

        when(repository.findAll(Optional.of("python"), Optional.empty()))
                .thenReturn(Collections.emptyList());
        when(staticFileService.loadStaticDiagrams())
                .thenReturn(List.of(fileDiagram));

        List<DiagramSummary> result = service.listAll(Optional.of("python"), Optional.empty());
        assertEquals(0, result.size());
    }

    @Test
    void listAll_filtersStaticByQuery() {
        Diagram fileDiagram = makeDiagram("file-test", "Microservice Flow", "file");

        when(repository.findAll(Optional.empty(), Optional.of("event")))
                .thenReturn(Collections.emptyList());
        when(staticFileService.loadStaticDiagrams())
                .thenReturn(List.of(fileDiagram));

        List<DiagramSummary> result = service.listAll(Optional.empty(), Optional.of("event"));
        assertEquals(0, result.size());
    }

    @Test
    void getById_returnsDbResult() {
        Diagram dbDiagram = makeDiagram("1", "DB Diagram", "db");
        when(repository.findById("1")).thenReturn(Optional.of(dbDiagram));

        Optional<Diagram> result = service.getById("1");
        assertTrue(result.isPresent());
        assertEquals("db", result.get().getSource());
        verify(staticFileService, never()).loadStaticDiagramById(any());
    }

    @Test
    void getById_fallsBackToStaticFile() {
        Diagram fileDiagram = makeDiagram("file-test", "File Diagram", "file");
        when(repository.findById("file-test")).thenReturn(Optional.empty());
        when(staticFileService.loadStaticDiagramById("file-test")).thenReturn(Optional.of(fileDiagram));

        Optional<Diagram> result = service.getById("file-test");
        assertTrue(result.isPresent());
        assertEquals("file", result.get().getSource());
    }

    @Test
    void getById_notFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        when(staticFileService.loadStaticDiagramById("missing")).thenReturn(Optional.empty());

        assertTrue(service.getById("missing").isEmpty());
    }

    @Test
    void create_setFieldsCorrectly() {
        ObjectNode flow = objectMapper.createObjectNode();
        flow.put("title", "Test");
        DiagramCreateRequest request = new DiagramCreateRequest("New Diagram", "Desc", List.of("tag1"), flow);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Diagram created = service.create(request);
        assertNotNull(created.getId());
        assertEquals("New Diagram", created.getTitle());
        assertEquals("Desc", created.getDescription());
        assertEquals(List.of("tag1"), created.getTags());
        assertEquals(1, created.getVersion());
        assertEquals("db", created.getSource());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    void create_nullTags_defaultsToEmptyList() {
        DiagramCreateRequest request = new DiagramCreateRequest("No Tags", null, null, null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Diagram created = service.create(request);
        assertEquals(Collections.emptyList(), created.getTags());
    }

    @Test
    void update_existingDiagram() {
        Diagram existing = makeDiagram("1", "Old Title", "db");
        existing.setVersion(1);
        when(repository.findById("1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DiagramUpdateRequest request = new DiagramUpdateRequest("New Title", "New Desc", List.of("updated"), null);
        Optional<Diagram> updated = service.update("1", request);

        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
        assertEquals(2, updated.get().getVersion());
    }

    @Test
    void update_notFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        DiagramUpdateRequest request = new DiagramUpdateRequest("Title", null, null, null);
        assertTrue(service.update("missing", request).isEmpty());
        verify(repository, never()).save(any());
    }

    private Diagram makeDiagram(String id, String title, String source) {
        Diagram d = new Diagram();
        d.setId(id);
        d.setTitle(title);
        d.setTags(Collections.emptyList());
        d.setVersion(1);
        d.setSource(source);
        d.setCreatedAt(Instant.now());
        d.setUpdatedAt(Instant.now());
        return d;
    }
}
