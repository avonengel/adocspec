package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;

public interface NodePipeFilter {
    Object handleNode(ContentNode node, ConversionContext context);
}
