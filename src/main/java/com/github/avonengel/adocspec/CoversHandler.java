package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.List;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class CoversHandler extends ListHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof List && context.getState() == SpecificationConverter.State.COVERS) {
            List list = (List) node;
            // [impl->dsn~oft-equivalent.covers~1]
            readSpecificationItemIdList(list, context.getSpecListBuilder()::addCoveredId);
            context.setState(SpecificationConverter.State.SPEC);
            return Optional.empty();
        }
        return null;
    }

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher coversMatcher = MdPattern.COVERS.getPattern().matcher(content);
        if (coversMatcher.matches()) {
            context.setState(SpecificationConverter.State.COVERS);
            return Optional.empty();
        }
        return null;
    }
}
