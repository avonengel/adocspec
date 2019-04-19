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

import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.core.Location;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.importer.ImportEventListener;
import org.itsallcode.openfasttrace.importer.SpecificationListBuilder;

import java.util.List;

public class BlockSpecListBuilder implements ImportEventListener {
    private final SpecificationListBuilder delegate;
    private boolean firstDescription = true;
    private boolean firstComment = true;
    private boolean firstRationale = true;

    public BlockSpecListBuilder(SpecificationListBuilder delegate) {
        this.delegate = delegate;
    }

    public List<SpecificationItem> build() {
        return delegate.build();
    }

    private void reset() {
        firstComment = true;
        firstDescription = true;
        firstRationale = true;
    }

    @Override
    public void beginSpecificationItem() {
        delegate.beginSpecificationItem();
        reset();
    }

    @Override
    public void setId(SpecificationItemId id) {
        delegate.setId(id);
    }

    @Override
    public void setTitle(String title) {
        delegate.setTitle(title);
    }

    @Override
    public void setStatus(ItemStatus status) {
        delegate.setStatus(status);
    }

    @Override
    public void appendDescription(String fragment) {
        if (!firstDescription) {
            delegate.appendDescription("\n\n");
        } else {
            firstDescription = false;
        }
        delegate.appendDescription(fragment);
    }

    @Override
    public void appendRationale(String fragment) {
        if (!firstRationale) {
            delegate.appendRationale("\n\n");
        } else {
            firstRationale = false;
        }
        delegate.appendRationale(fragment);
    }

    @Override
    public void appendComment(String fragment) {
        if (!firstComment) {
            delegate.appendComment("\n\n");
        } else {
            firstComment = false;
        }
        delegate.appendComment(fragment);
    }

    @Override
    public void addCoveredId(SpecificationItemId id) {
        delegate.addCoveredId(id);
    }

    @Override
    public void addDependsOnId(SpecificationItemId id) {
        delegate.addDependsOnId(id);
    }

    @Override
    public void addNeededArtifactType(String artifactType) {
        delegate.addNeededArtifactType(artifactType);
    }

    @Override
    public void addTag(String tag) {
        delegate.addTag(tag);
    }

    @Override
    public void setLocation(String path, int line) {
        delegate.setLocation(path, line);
    }

    @Override
    public void endSpecificationItem() {
        delegate.endSpecificationItem();
    }

    @Override
    public void setLocation(Location location) {
        delegate.setLocation(location);
    }

    @Override
    public void setForwards(boolean forwards) {
        delegate.setForwards(forwards);
    }
}
