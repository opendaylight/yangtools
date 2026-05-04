/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

public final class RuntimeBitsTypeObjectArchetypeBuilder extends AbstractGeneratedTOBuilder
        implements BitsTypeObjectArchetype.Builder {
    @NonNullByDefault
    public RuntimeBitsTypeObjectArchetypeBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public BitsTypeObjectArchetype build() {
        return new BitsGTO(this);
    }

    private static final class BitsGTO extends AbstractGeneratedTransferObject implements BitsTypeObjectArchetype {
        BitsGTO(final RuntimeBitsTypeObjectArchetypeBuilder builder) {
            super(builder);
        }
    }
}
