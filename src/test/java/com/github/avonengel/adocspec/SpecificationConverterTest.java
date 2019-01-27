package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.assertj.core.api.Assertions.assertThat;

class SpecificationConverterTest {
    private static final String A_TYPE = "dummy";
    private static final String AN_OTHER_TYPE = "othertype";
    private static final String A_SPEC_NAME = "a-spec";
    private static final int A_SPEC_VERSION = 3;
    private static final SpecificationItemId A_SPEC_ID = SpecificationItemId.createId(A_TYPE, A_SPEC_NAME, A_SPEC_VERSION);
    private static final String A_DESCRIPTION = "some text content, to be used as description";
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
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
    }

    @Test
    @DisplayName("When a Spec ID is found inside a section, then a Spec item is created")
    void whenIdIsFoundInSectionSpecIsCreated() {
        // Arrange
        String input = "== A Section Title\n" +
                "`+" + A_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
    }

    @SuppressWarnings("unchecked")
    private List<SpecificationItem> convertToSpecList(String input) {
        return asciidoctor.convert(input, OptionsBuilder.options().backend("spec"), List.class);
    }

    @DisplayName("Given a specification item ID")
    @Nested
    class GivenID {

        @Test
        @DisplayName("When a paragraph without any markers is found, then the paragraph becomes the spec's description")
        void whenParagraphThenDescription() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    A_DESCRIPTION;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getDescription).containsOnly(A_DESCRIPTION);
        }

        @Test
        @DisplayName("When a paragraph that starts with 'Needs:', then the spec's needs are set")
        void whenParagraphStartsWithNeedsThenNeedsIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Needs: " + AN_OTHER_TYPE;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getNeedsArtifactTypes)
                    .first().asList().containsOnly(AN_OTHER_TYPE);
        }
    }
}