package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;

public interface NodeHandler<T> {
    NodeResult<T> handleNode(ContentNode node, ConversionContext context);
}
