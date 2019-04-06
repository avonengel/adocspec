package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class TagsHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher tagsMatcher = MdPattern.TAGS_INT.getPattern().matcher(content);
        if (tagsMatcher.matches()) {
            // [impl->dsn~oft-equivalent.tags~1]
            for (final String tag : tagsMatcher.group(1).split(",\\s*")) {
                context.getSpecListBuilder().addTag(tag);
            }
            return Optional.empty();
        }
        return null;
    }
}
