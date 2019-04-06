package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class StatusHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher statusMatcher = MdPattern.STATUS.getPattern().matcher(content);

        if (statusMatcher.matches()) {
            // [impl->dsn~oft-equivalent.status~1]
            context.getSpecListBuilder().setStatus(ItemStatus.parseString(statusMatcher.group(1)));
            return Optional.empty();
        }
        return null;
    }
}
