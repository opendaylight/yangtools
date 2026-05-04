/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;

public final class RuntimeScalarTypeObjectArchetypeBuilder extends AbstractGeneratedTOBuilder
        implements ScalarTypeObjectArchetype.Builder {
    @NonNullByDefault
    public RuntimeScalarTypeObjectArchetypeBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public ScalarTypeObjectArchetype build() {
        return new ScalarGTO(this);
    }

    private static final class ScalarGTO extends AbstractGeneratedTransferObject implements ScalarTypeObjectArchetype {
        ScalarGTO(final RuntimeScalarTypeObjectArchetypeBuilder builder) {
            super(builder);
        }
    }
}
