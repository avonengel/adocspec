package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.List;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class DependsHandler extends ListHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof List && context.getState() == SpecificationConverter.State.DEPENDS) {
            List list = (List) node;
            // [impl->dsn~oft-equivalent.depends-list~1]
            readSpecificationItemIdList(list, context.getSpecListBuilder()::addDependsOnId);
            context.setState(SpecificationConverter.State.SPEC);
            return Optional.empty();
        }

        return null;

    }

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher dependsMatcher = MdPattern.DEPENDS.getPattern().matcher(content);
        if (dependsMatcher.matches()) {
            context.setState(SpecificationConverter.State.DEPENDS);
        }
        return Optional.empty();
    }
}
