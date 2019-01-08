package com.github.avonengel.adocspec;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;

import java.util.Map;

public class SpecBlockProcessor extends BlockProcessor {
    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        System.out.println(String.join("\n-", reader.readLines()));
        return createSection(parent);
    }
}
