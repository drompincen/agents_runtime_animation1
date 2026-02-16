package io.github.drompincen.archviz.repository;

import io.github.drompincen.archviz.model.Diagram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDiagramRepositoryTest {

    private InMemoryDiagramRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryDiagramRepository();
    }

    @Test
    void save_and_findById() {
        Diagram d = makeDiagram("1", "Test Diagram", List.of("tag1"));
        repo.save(d);

        Optional<Diagram> found = repo.findById("1");
        assertTrue(found.isPresent());
        assertEquals("Test Diagram", found.get().getTitle());
    }

    @Test
    void findById_notFound() {
        assertTrue(repo.findById("nonexistent").isEmpty());
    }

    @Test
    void findAll_noFilters() {
        repo.save(makeDiagram("1", "Alpha", List.of("a")));
        repo.save(makeDiagram("2", "Beta", List.of("b")));

        List<Diagram> all = repo.findAll(Optional.empty(), Optional.empty());
        assertEquals(2, all.size());
    }

    @Test
    void findAll_filterByTag() {
        repo.save(makeDiagram("1", "Alpha", List.of("java", "spring")));
        repo.save(makeDiagram("2", "Beta", List.of("python")));

        List<Diagram> filtered = repo.findAll(Optional.of("java"), Optional.empty());
        assertEquals(1, filtered.size());
        assertEquals("Alpha", filtered.get(0).getTitle());
    }

    @Test
    void findAll_filterByQuery() {
        repo.save(makeDiagram("1", "Microservice Architecture", List.of()));
        repo.save(makeDiagram("2", "Event Pipeline", List.of()));

        List<Diagram> filtered = repo.findAll(Optional.empty(), Optional.of("micro"));
        assertEquals(1, filtered.size());
        assertEquals("Microservice Architecture", filtered.get(0).getTitle());
    }

    @Test
    void findAll_filterByQuery_caseInsensitive() {
        repo.save(makeDiagram("1", "Microservice Architecture", List.of()));

        List<Diagram> filtered = repo.findAll(Optional.empty(), Optional.of("MICRO"));
        assertEquals(1, filtered.size());
    }

    @Test
    void findAll_filterByTagAndQuery() {
        repo.save(makeDiagram("1", "Microservice Architecture", List.of("java")));
        repo.save(makeDiagram("2", "Microservice Pipeline", List.of("python")));

        List<Diagram> filtered = repo.findAll(Optional.of("java"), Optional.of("micro"));
        assertEquals(1, filtered.size());
        assertEquals("1", filtered.get(0).getId());
    }

    @Test
    void deleteById() {
        repo.save(makeDiagram("1", "Test", List.of()));
        repo.deleteById("1");
        assertTrue(repo.findById("1").isEmpty());
    }

    @Test
    void save_overwritesExisting() {
        repo.save(makeDiagram("1", "Original", List.of()));
        repo.save(makeDiagram("1", "Updated", List.of()));

        assertEquals("Updated", repo.findById("1").get().getTitle());
    }

    private Diagram makeDiagram(String id, String title, List<String> tags) {
        Diagram d = new Diagram();
        d.setId(id);
        d.setTitle(title);
        d.setTags(tags);
        d.setVersion(1);
        d.setSource("db");
        d.setCreatedAt(Instant.now());
        d.setUpdatedAt(Instant.now());
        return d;
    }
}
