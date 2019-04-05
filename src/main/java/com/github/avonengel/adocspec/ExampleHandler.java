package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;

public class ExampleHandler implements NodeContentHandler<String> {
    @Override
    public NodeResult<String> handleNode(ContentNode node, String content, ConversionContext context) {
        if (inExample(node)) {
            return NodeResult.of(content);
        } else if ("example".equals(node.getNodeName())) {
            appendTextBlock(content, context);
            // how to signal to the caller that this block has been handled
            // and no further handlers should be called?
            return NodeResult.stopHandlers();
        }
        return NodeResult.continueHandlers();
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
