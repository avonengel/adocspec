package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.spi.ExtensionRegistry;

public class SpecExtension implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {

        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        // tag::register[]
        javaExtensionRegistry.preprocessor(SourcemapPreprocessor.class);
        javaExtensionRegistry.treeprocessor(SpecTreeProcessor.class);
        asciidoctor.javaConverterRegistry().register(SpecificationConverter.class, "spec");
        // end::register[]
    }

}