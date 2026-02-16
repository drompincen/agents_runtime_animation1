package io.github.drompincen.archviz.service;

import io.github.drompincen.archviz.model.Diagram;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StaticFileServiceTest {

    @Autowired
    private StaticFileService staticFileService;

    @Test
    void loadStaticDiagrams_returnsNonEmptyList() {
        List<Diagram> diagrams = staticFileService.loadStaticDiagrams();
        assertFalse(diagrams.isEmpty(), "Should load at least one static JSON file");
    }

    @Test
    void loadStaticDiagrams_setsCorrectSource() {
        List<Diagram> diagrams = staticFileService.loadStaticDiagrams();
        diagrams.forEach(d -> assertEquals("file", d.getSource()));
    }

    @Test
    void loadStaticDiagrams_idStartsWithFilePrefix() {
        List<Diagram> diagrams = staticFileService.loadStaticDiagrams();
        diagrams.forEach(d -> assertTrue(d.getId().startsWith("file-"),
                "Static diagram ID should start with 'file-': " + d.getId()));
    }

    @Test
    void loadStaticDiagrams_hasFlowData() {
        List<Diagram> diagrams = staticFileService.loadStaticDiagrams();
        diagrams.forEach(d -> assertNotNull(d.getFlow(), "Flow should not be null for: " + d.getId()));
    }

    @Test
    void loadStaticDiagramById_found() {
        List<Diagram> all = staticFileService.loadStaticDiagrams();
        if (!all.isEmpty()) {
            String firstId = all.get(0).getId();
            Optional<Diagram> found = staticFileService.loadStaticDiagramById(firstId);
            assertTrue(found.isPresent());
            assertEquals(firstId, found.get().getId());
        }
    }

    @Test
    void loadStaticDiagramById_notFound() {
        Optional<Diagram> found = staticFileService.loadStaticDiagramById("file-nonexistent");
        assertTrue(found.isEmpty());
    }
}
