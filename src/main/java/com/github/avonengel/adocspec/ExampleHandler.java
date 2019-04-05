package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;

import java.util.Optional;

public class ExampleHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        if (inExample(block)) {
            return Optional.of(content);
        } else if ("example".equals(block.getNodeName())) {
            appendTextBlock(content, context);
            // how to signal to the caller that this block has been handled
            // and no further handlers should be called?
            return Optional.empty();
        }
        return null;
    }

    private boolean inExample(ContentNode block) {
        ContentNode current = block.getParent();
        while (current != null) {
            if ("example".equals(current.getNodeName())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

}
