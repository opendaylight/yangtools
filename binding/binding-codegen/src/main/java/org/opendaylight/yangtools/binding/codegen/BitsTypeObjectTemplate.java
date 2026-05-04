/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;

/**
 * A template for {@link BitsTypeObject} specializations.
 */
final class BitsTypeObjectTemplate extends ClassTemplate {
    @NonNullByDefault
    private BitsTypeObjectTemplate(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype) {
        super(javaType, archetype);
    }

    @NonNullByDefault
    BitsTypeObjectTemplate(final BitsTypeObjectArchetype archetype) {
        super(archetype);
    }

    @NonNullByDefault
    static BlockBuilder generateAsInner(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype) {
        return new BitsTypeObjectTemplate(javaType, archetype).generateAsInnerClass();
    }
}
