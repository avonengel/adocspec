package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import java.util.Map;

public class SourcemapPreprocessor extends Preprocessor {
    public SourcemapPreprocessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {
        document.setSourcemap(true);
    }
}
