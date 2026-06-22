/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder.GTO;

@NonNullByDefault
final class CodegenUnionTO extends GTO<UnionTypeObject> implements UnionTypeObjectArchetype {
    private final List<String> typePropertyNames;

    CodegenUnionTO(final CodegenGeneratedTOBuilder builder, final List<String> typePropertyNames) {
        super(builder);
        this.typePropertyNames = requireNonNull(typePropertyNames);
    }

    @Override
    public List<String> typePropertyNames() {
        return typePropertyNames;
    }

    @Override
    public long serialVersionUID() {
        return UnionTypeObjectArchetype.super.serialVersionUID();
    }
}
