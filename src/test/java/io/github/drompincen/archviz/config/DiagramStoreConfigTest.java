package io.github.drompincen.archviz.config;

import io.github.drompincen.archviz.repository.DiagramRepository;
import io.github.drompincen.archviz.repository.InMemoryDiagramRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

class DiagramStoreConfigTest {

    @SpringBootTest(properties = {"diagram.store=inMemory"})
    @TestPropertySource(properties = {"diagram.store=inMemory"})
    static class InMemoryConfigTest {

        @Autowired
        private DiagramRepository repository;

        @Test
        void inMemoryStoreIsDefault() {
            assertInstanceOf(InMemoryDiagramRepository.class, repository);
        }
    }

    @SpringBootTest
    static class DefaultConfigTest {

        @Autowired
        private DiagramRepository repository;

        @Test
        void defaultStoreIsInMemory() {
            assertInstanceOf(InMemoryDiagramRepository.class, repository);
        }
    }
}
