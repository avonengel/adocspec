package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;

import java.util.Optional;

public class UnqualifiedBlockHandler implements BlockContentHandler {

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        appendTextBlock(content, context);
        return null;
    }

}
