/**
 * adocspec - AsciidoctorJ extension to specify requirements via OpenFastTrace
 * Copyright (C) 2019 Axel von Engel <a.vonengel@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A SpecObject")
class SpecTreeProcessorTest {

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
        asciidoctor.convert(input, options().toFile(false));

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
            asciidoctor.convert(input, options().toFile(false));

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
        @DisplayName("when section body exists then it becomes the Description")
        void whenSectionBodyExists() {
            // Arrange
            String dummyDescription = "Specification description should go here. Describes what is required.";
            String input = "[.spec,specID=" + TEST_ID + "]\n" +
                    "== A specification section\n" +
                    dummyDescription;

            // Act
            asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects)
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
            asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            assertThat(item.getNeedsArtifactTypes()).containsOnly(dummyType);
            assertThat(item.getDescription()).isNullOrEmpty();
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
            asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            assertThat(item.getDescription()).isNullOrEmpty();
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
            asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            assertThat(item.getDescription()).isNullOrEmpty();
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
            asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            assertThat(item.getDescription()).isNullOrEmpty();
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
            asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects).hasSize(1);
            SpecificationItem item = specObjects.get(0);
            assertThat(item.getCoveredIds()).containsOnly(SpecificationItemId.parseId(dummyCovers));
            assertThat(item.getDescription()).isNullOrEmpty();
        }
    }
}
