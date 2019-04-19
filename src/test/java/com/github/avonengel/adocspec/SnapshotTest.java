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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.SafeMode;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.itsallcode.openfasttrace.ImportSettings;
import org.itsallcode.openfasttrace.Oft;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;
import static org.assertj.core.api.Assertions.assertThat;

class SnapshotTest {

    private static Asciidoctor asciidoctor;

    @BeforeAll
    static void prepareAsciidoctor() {
        asciidoctor = create();
    }

    @ParameterizedTest
    @ValueSource(strings = {"oft/design", "oft/system_requirements"})
    void testSnapshot(String filePath, @TempDir File destinationDir) throws Exception {
        // Arrange
        URL resource = SnapshotTest.class.getResource("/" + filePath + ".adoc");
        final ImportSettings settings = ImportSettings.builder().addInputs(Paths.get(SnapshotTest.class.getResource("/" + filePath + ".xml").toURI())).build();
        Oft oft = Oft.create();
        final List<SpecificationItem> expectedResult = oft.importItems(settings);

        // Act
        asciidoctor.convertFile(Paths.get(resource.toURI()).toFile(), options().safe(SafeMode.UNSAFE).backend("spec").toDir(destinationDir));

        // Assert
        List<SpecificationItem> actualResult = oft.importItems(ImportSettings.builder().addInputs(destinationDir.toPath()).build());

        assertThat(actualResult.size()).as("result size").isEqualTo(expectedResult.size());
        Assertions.registerFormatterForType(SpecificationItem.class, s -> new ReflectionToStringBuilder(s).build());
        SoftAssertions softly = new SoftAssertions();
        for (int i = 0; i < expectedResult.size(); i++) {
            SpecificationItem expectedItem = expectedResult.get(i);
            SpecificationItem actualItem = actualResult.get(i);
            softly.assertThat(actualItem).isEqualToIgnoringGivenFields(expectedItem, "location");
        }
        softly.assertAll();
    }
}
