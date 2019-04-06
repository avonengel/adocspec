package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class NeedsHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher needsMatcher = MdPattern.NEEDS_INT.getPattern().matcher(content);
        if (needsMatcher.matches()) {
            // [impl->dsn~oft-equivalent.needs~1]
            for (final String artifactType : needsMatcher.group(1).split(",\\s*")) {
                context.getSpecListBuilder().addNeededArtifactType(artifactType);
            }

            return Optional.empty();
        }
        return null;
    }
}
