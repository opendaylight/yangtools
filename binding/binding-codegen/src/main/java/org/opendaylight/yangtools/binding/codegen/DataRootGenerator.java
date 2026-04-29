/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;

@NonNullByDefault
record DataRootGenerator(DataRootArchetype type) implements Generator {
    DataRootGenerator(final DataRootArchetype type) {
        this.type = requireNonNull(type);
    }

    @Override
    public DataRootArchetype type() {
        return type;
    }

    @Override
    public String generate() {
        return new DataRootTemplate(type).generate();
    }
}
