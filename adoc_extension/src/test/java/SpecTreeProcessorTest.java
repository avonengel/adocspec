import com.github.avonengel.adocspec.SpecTreeProcessor;
import org.asciidoctor.Asciidoctor;

import static java.util.stream.Collectors.toList;
import static org.asciidoctor.OptionsBuilder.options;

import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("A SpecObject")
public class SpecTreeProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SpecTreeProcessorTest.class);
    private static final SpecificationItemId TEST_ID = SpecificationItemId.createId("tst", "test-spec-id", 1);

    private static Asciidoctor asciidoctor;
    private SpecTreeProcessor underTest;

    @BeforeAll
    static void prepareAsciidoctor() {
        asciidoctor = create();
    }

    @BeforeEach
    void prepareSpecProcessor() {
        asciidoctor.unregisterAllExtensions();
        underTest = new SpecTreeProcessor();
        asciidoctor.javaExtensionRegistry().treeprocessor(underTest);
    }

    @Test
    @DisplayName("is defined by using the .spec role")
    public void whenSpecRoleIsFoundOnSectionSpecItemIsCreated() {
        // Arrange
        String input = "[.spec,specID=" + TEST_ID + "]\n" +
                "== A specification section";

        // Act
        String output = asciidoctor.convert(input, options().toFile(false));

        // Assert
        List<SpecificationItem> specObjects = underTest.getSpecObjects();
        assertThat(specObjects, notNullValue());
        assertThat(specObjects, not(empty()));
    }

    @DisplayName("has a SpecificationItemId")
    @Nested
    class SpecificationItemIdTest {
        @Test
        @DisplayName("that can be defined in OFT syntax via specID attribute")
        void specItemIdCanBeDefinedInOftSyntax() {
            // Arrange
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section";

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects.stream()
                            .map(SpecificationItem::getId)
                            .collect(toList()),
                    hasItem(TEST_ID));
        }
    }
}
