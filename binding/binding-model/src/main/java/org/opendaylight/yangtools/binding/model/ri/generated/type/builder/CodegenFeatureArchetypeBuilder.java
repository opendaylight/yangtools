/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

@NonNullByDefault
public final class CodegenFeatureArchetypeBuilder extends CodegenGeneratedTOBuilder
        implements FeatureArchetype.Builder {
    public CodegenFeatureArchetypeBuilder(final JavaTypeName typeName, final JavaTypeName rootTypeName) {
        super(typeName);
        addImplementsType(BindingTypes.yangFeature(TypeRef.of(typeName), TypeRef.of(rootTypeName)));
    }

    @Override
    public FeatureArchetype build() {
        return new FeatureGTO(this);
    }

    private static final class FeatureGTO extends GTO implements FeatureArchetype {
        FeatureGTO(final CodegenGeneratedTOBuilder builder) {
            super(builder);
        }
    }
}
