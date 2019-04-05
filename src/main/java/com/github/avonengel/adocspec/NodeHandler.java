package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;

import java.util.Optional;

public interface NodeHandler {
    Optional<Object> handleNode(ContentNode node, ConversionContext context);
}
