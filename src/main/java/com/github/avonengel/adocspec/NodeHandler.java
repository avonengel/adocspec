package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;

public interface NodeHandler {
    Object handleNode(ContentNode node, ConversionContext context);
}
