/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public final class LegacyArchetypeBuilder<S extends EffectiveStatement<?, ?>>
        extends AbstractGeneratedTypeBuilder<LegacyArchetypeBuilder<S>, S> {
    @NonNullByDefault
    public LegacyArchetypeBuilder(final JavaTypeName typeName, final S statement) {
        super(typeName, statement);
    }

    @Override
    public LegacyArchetype<S> build() {
        return new DefaultLegacyArchetype<>(this);
    }

    @Override
    protected LegacyArchetypeBuilder<S> thisInstance() {
        return this;
    }
}
