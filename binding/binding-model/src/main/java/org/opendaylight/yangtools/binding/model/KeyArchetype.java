/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * An archetype for a {@link Key}.
 */
@NonNullByDefault
public final class KeyArchetype extends Archetype<KeyArchetype> {
    public KeyArchetype(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public Class<KeyArchetype> archetypeContract() {
        return KeyArchetype.class;
    }
}
