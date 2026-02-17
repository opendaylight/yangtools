/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;

@NonNullByDefault
record DefaultSourceSet(
        SourceInfoRef.OfModule module,
        List<SourceInfoRef.OfSubmodule> submodules) implements SourceSet {
    DefaultSourceSet {
        requireNonNull(module);
        submodules = List.copyOf(submodules);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SourceSet.class).omitEmptyValues()
            .add("module", module)
            .add("submodules", submodules)
            .toString();
    }
}
