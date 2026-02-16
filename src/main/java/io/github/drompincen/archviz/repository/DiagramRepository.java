package io.github.drompincen.archviz.repository;

import io.github.drompincen.archviz.model.Diagram;

import java.util.List;
import java.util.Optional;

public interface DiagramRepository {

    Diagram save(Diagram diagram);

    Optional<Diagram> findById(String id);

    List<Diagram> findAll(Optional<String> tag, Optional<String> query);

    void deleteById(String id);
}
