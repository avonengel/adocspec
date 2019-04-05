package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SectionHandler implements NodeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SectionHandler.class);

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Section) {
            final Section section = (Section) node;
            LOG.info("Processing section {}", section.getTitle());
            context.getSpecListBuilder().endSpecificationItem();
            section.getBlocks().forEach(StructuralNode::convert);
            context.getSpecListBuilder().endSpecificationItem();
            context.setState(SpecificationConverter.State.START);
            return Optional.empty();
        }
        return null;
    }
}
