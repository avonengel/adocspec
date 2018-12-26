import com.github.avonengel.adocspec.SpecTreeProcessor;
import org.asciidoctor.Asciidoctor;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A SpecObject")
class SpecTreeProcessorTest {

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
    void whenSpecRoleIsFoundOnSectionSpecItemIsCreated() {
        // Arrange
        String input = "[.spec,specID=" + TEST_ID + "]\n" +
                "== A specification section";

        // Act
        String output = asciidoctor.convert(input, options().toFile(false));

        // Assert
        List<SpecificationItem> specObjects = underTest.getSpecObjects();

        assertThat(specObjects).isNotNull()
                .isNotEmpty();
    }

    @DisplayName("has a SpecificationItemId")
    @Nested
    class HasId {
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
            assertThat(specObjects)
                    .extracting(SpecificationItem::getId)
                    .contains(TEST_ID);
        }
    }

    @DisplayName("has a Description")
    @Nested
    class HasDescription {
        @Test
        @DisplayName("when section body exists")
        void whenSectionBodyExists() {
            // Arrange
            String dummyDescription = "Specification description should go here. Describes what is required.";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    dummyDescription;

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects)
                    .extracting(SpecificationItem::getDescription)
                    .contains(dummyDescription);
        }
    }
}
