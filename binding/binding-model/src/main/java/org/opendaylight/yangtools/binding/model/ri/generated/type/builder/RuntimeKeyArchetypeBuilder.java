/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;

public final class RuntimeKeyArchetypeBuilder extends RuntimeGeneratedTOBuilder implements KeyArchetype.Builder {
    public RuntimeKeyArchetypeBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public KeyArchetype build() {
        return new KeyGTO(this);
    }

    private static final class KeyGTO extends GTO implements KeyArchetype {
        KeyGTO(final RuntimeKeyArchetypeBuilder builder) {
            super(builder);
        }
    }
}
