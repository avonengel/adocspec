package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;

public interface NodeContentHandler<T> {
    public NodeResult<T> handleNode(ContentNode node, String content, ConversionContext context);

    default void appendTextBlock(String content, ConversionContext context) {
        if (content.trim().isEmpty()) {
            return;
        }
        if (context.getState() == SpecificationConverter.State.SPEC || context.getState() == SpecificationConverter.State.DESCRIPTION) {
            // [impl->dsn~oft-equivalent.description~2]
            context.getSpecListBuilder().appendDescription(content);
        } else if (context.getState() == SpecificationConverter.State.COMMENT) {
            context.getSpecListBuilder().appendComment(content);
        } else if (context.getState() == SpecificationConverter.State.RATIONALE) {
            context.getSpecListBuilder().appendRationale(content);
        }
    }

}