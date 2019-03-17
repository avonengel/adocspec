package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SectionPipeFilter implements NodePipeFilter {
    private static final Logger LOG = LoggerFactory.getLogger(SectionPipeFilter.class);

    @Override
    public Object handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Section) {
            final Section section = (Section) node;
            LOG.info("Processing section {}", section.getTitle());
            context.getSpecListBuilder().endSpecificationItem();
            section.getBlocks().forEach(StructuralNode::convert);
            context.getSpecListBuilder().endSpecificationItem();
            context.setState(SpecificationConverter.State.START);
        }
        return null;
    }
}
