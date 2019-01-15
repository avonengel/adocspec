package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.assertj.core.api.Assertions.assertThat;

class SpecificationConverterTest {

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
    void specConversionTest() {
        // Arrange
        String input = "# Document title\n\n"
                + "## first section\n\n"
                + "first paragraph {foo}\n\n"
                + "second paragraph with `+dummy~dummy-id~1+`";

        // Act
        final AttributesBuilder attributes = attributes().attribute("foo", "bar");
        final String output = asciidoctor.convert(input, OptionsBuilder.options().backend("spec").attributes(attributes));

        // Assert
        assertThat(output).isNotBlank();
        Assertions.fail(output);
    }
}