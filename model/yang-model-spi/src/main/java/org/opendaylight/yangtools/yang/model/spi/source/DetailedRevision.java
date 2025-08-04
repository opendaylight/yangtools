/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;

public class DetailedRevision {

    private final Referenced<Revision> revision;
    private final Referenced<String> description;
    private final Referenced<String> reference;

    public static DetailedRevision of(final Referenced<String> revision, final Referenced<String> descr,
        final Referenced<String> reference) {
        return new DetailedRevision(new Referenced<>(Revision.of(revision.value()), revision.reference()), descr, reference);
    }

    public DetailedRevision(final @NonNull Referenced<Revision> revision,
        final @Nullable Referenced<String> description, @Nullable Referenced<String> reference) {
        this.revision = requireNonNull(revision);
        this.description = description;
        this.reference = reference;
    }

    public Referenced<Revision> getRevision() {
        return revision;
    }

    public Referenced<String> getDescription() {
        return description;
    }

    public Referenced<String> getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DetailedRevision other) {
            return other.getRevision().value().equals(this.revision.value());
        }
        return false;
    }
}
