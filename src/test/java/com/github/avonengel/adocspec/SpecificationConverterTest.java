package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.assertj.core.api.Assertions.assertThat;

class SpecificationConverterTest {
    private static final String AN_ARTIFACT_TYPE = "dummy";
    private static final String A_SPEC_NAME = "a-spec";
    private static final int A_SPEC_VERSION = 3;
    private static final SpecificationItemId A_SPEC_ID = SpecificationItemId.createId(AN_ARTIFACT_TYPE, A_SPEC_NAME, A_SPEC_VERSION);
    private static Asciidoctor asciidoctor;

    @BeforeAll
    static void prepareAsciidoctor() {
        asciidoctor = create();
    }

    @BeforeEach
    void prepareSpecProcessor() {
        asciidoctor.unregisterAllExtensions();
        asciidoctor.javaConverterRegistry().register(SpecificationConverter.class, "spec");
    }


    @Test
    @DisplayName("When a Spec ID is found, then a Spec item is created")
    void whenIdIsFoundSpecIsCreated() {
        // Arrange
        String input = "`+" + A_SPEC_ID + "+`";

        // Act
        final String output = asciidoctor.convert(input, OptionsBuilder.options().backend("spec"));

        // Assert
        assertThat(output).isNotBlank();
        Assertions.fail(output);
    }
}