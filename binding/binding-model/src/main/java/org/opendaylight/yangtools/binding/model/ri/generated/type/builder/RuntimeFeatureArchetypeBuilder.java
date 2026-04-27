/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

@NonNullByDefault
public final class RuntimeFeatureArchetypeBuilder extends RuntimeGeneratedTOBuilder
        implements FeatureArchetype.Builder {
    private final JavaTypeName dataRoot;

    public RuntimeFeatureArchetypeBuilder(final JavaTypeName typeName, final JavaTypeName dataRoot) {
        super(typeName);
        this.dataRoot = requireNonNull(dataRoot);
    }

    @Override
    public FeatureArchetype build() {
        return new FeatureGTO(this, dataRoot);
    }

    private static final class FeatureGTO extends GTO implements FeatureArchetype {
        private final JavaTypeName dataRoot;

        FeatureGTO(final RuntimeGeneratedTOBuilder builder, final JavaTypeName dataRoot) {
            super(builder);
            this.dataRoot = requireNonNull(dataRoot);
        }

        @Override
        public JavaTypeName dataRoot() {
            return dataRoot;
        }
    }
}
