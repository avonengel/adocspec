package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.assertj.core.api.Assertions;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;

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

        Assertions.assertThat(specObjects).isNotNull()
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
            Assertions.assertThat(specObjects)
                    .extracting(SpecificationItem::getId)
                    .contains(TEST_ID);
        }
    }

    @DisplayName("has a Description")
    @Nested
    class HasDescription {
        @Test
        @DisplayName("when section body exists then it becomes the Description")
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
            Assertions.assertThat(specObjects)
                    .extracting(SpecificationItem::getDescription)
                    .contains(dummyDescription);
        }
    }

    @DisplayName("may have coverage Needs")
    @Nested
    class SpecifiesCoverage {
        @Test
        @DisplayName("specified like in OFT: 'Needs: dsn'")
        void needsParagraphWithInlineList() {
            // Arrange
            final String dummyType = "dummy";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    "Needs: " + dummyType;

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            Assertions.assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            Assertions.assertThat(item.getNeedsArtifactTypes()).containsOnly(dummyType);
            Assertions.assertThat(item.getDescription()).isNullOrEmpty();
        }

        @DisplayName("specified as a list")
        void needsList() {
            /*
             TODO: later:
             Needs:
             * dummy1
             * dummy2

             or even:
             .Needs
             * dummy1
             * dummy2

             or:
             [.needs]
             * dummy1
             * dummy2
             */
        }
    }

    @DisplayName("may cover requirements")
    @Nested
    class HasCoversList {
        @DisplayName("defined using role .covers with $$")
        @Test
        void coversListWithRoleWithDoubleDollarPassStyle() {
            // Arrange
            final String dummyCovers = "dummy~some-dummy-id~1";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    "[.covers]\n" +
                    "* $$" + dummyCovers + "$$";

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            Assertions.assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            Assertions.assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            Assertions.assertThat(item.getDescription()).isNullOrEmpty();
        }

        @DisplayName("defined using role .covers with +++")
        @Test
        void coversListWithRoleWithTriplePlusPassStyle() {
            // Arrange
            final String dummyCovers = "dummy~some-dummy-id~1";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    "[.covers]\n" +
                    "* +++" + dummyCovers + "+++";

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            Assertions.assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            Assertions.assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            Assertions.assertThat(item.getDescription()).isNullOrEmpty();
        }

        @DisplayName("defined using role .covers with pass:[]")
        @Test
        void coversListWithRoleWithPassInlineMacro() {
            // Arrange
            final String dummyCovers = "dummy~some-dummy-id~1";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    "[.covers]\n" +
                    "* pass:[" + dummyCovers + "]";

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            Assertions.assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            Assertions.assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            Assertions.assertThat(item.getDescription()).isNullOrEmpty();
        }

        @DisplayName("defined using role .covers with ++")
        @Test
        void coversListWithRoleWithDoublePlusPass() {
            // Arrange
            final String dummyCovers = "dummy~some-dummy-id~1";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    "[.covers]\n" +
                    "* ++" + dummyCovers + "++";

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            Assertions.assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            Assertions.assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            Assertions.assertThat(item.getDescription()).isNullOrEmpty();
        }

        @DisplayName("defined using role .covers with `+")
        @Test
        void coversListWithRoleWithBacktickPlusPass() {
            // Arrange
            final String dummyCovers = "dummy~some-dummy-id~1";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    "[.covers]\n" +
                    "* `+" + dummyCovers + "+`";

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            Assertions.assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            Assertions.assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            Assertions.assertThat(item.getDescription()).isNullOrEmpty();
        }
    }
}