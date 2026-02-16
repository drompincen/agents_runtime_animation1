package io.github.drompincen.archviz.repository;

import io.github.drompincen.archviz.LocalDynamoDbExtension;
import io.github.drompincen.archviz.model.Diagram;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DynamoDiagramRepositoryTest {

    @RegisterExtension
    static LocalDynamoDbExtension dynamoDb = new LocalDynamoDbExtension();

    private DynamoDiagramRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        DynamoDbClient client = DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoDb.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("fakeKey", "fakeSecret")))
                .build();

        // Use a unique table name per test to avoid state leaking
        String tableName = "test-diagrams-" + System.nanoTime();
        repo = new DynamoDiagramRepository(client, tableName);
    }

    @Test
    void save_and_findById() {
        Diagram d = makeDiagram("1", "Test Diagram", List.of("tag1"));
        repo.save(d);

        Optional<Diagram> found = repo.findById("1");
        assertTrue(found.isPresent());
        assertEquals("Test Diagram", found.get().getTitle());
        assertEquals(List.of("tag1"), found.get().getTags());
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

        List<Diagram> filtered = repo.findAll(Optional.empty(), Optional.of("Microservice"));
        assertEquals(1, filtered.size());
    }

    @Test
    void deleteById() {
        repo.save(makeDiagram("1", "Test", List.of()));
        repo.deleteById("1");
        assertTrue(repo.findById("1").isEmpty());
    }

    @Test
    void save_withFlow() {
        Diagram d = makeDiagram("1", "Flow Test", List.of());
        ObjectNode flow = objectMapper.createObjectNode();
        flow.put("title", "Test Flow");
        flow.putArray("nodes").addObject().put("id", "n1");
        d.setFlow(flow);

        repo.save(d);
        Optional<Diagram> found = repo.findById("1");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getFlow());
        assertEquals("Test Flow", found.get().getFlow().get("title").asText());
    }

    @Test
    void save_preservesTimestamps() {
        Instant now = Instant.parse("2025-01-15T10:30:00Z");
        Diagram d = makeDiagram("1", "Timestamp Test", List.of());
        d.setCreatedAt(now);
        d.setUpdatedAt(now);

        repo.save(d);
        Optional<Diagram> found = repo.findById("1");
        assertTrue(found.isPresent());
        assertEquals(now, found.get().getCreatedAt());
        assertEquals(now, found.get().getUpdatedAt());
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
