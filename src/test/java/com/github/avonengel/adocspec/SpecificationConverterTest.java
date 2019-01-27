package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
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
        final List<SpecificationItem> output = asciidoctor.convert(input, OptionsBuilder.options().backend("spec"), List.class);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
    }
}