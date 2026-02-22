/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype.Pair;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractPair.RuntimePair;
import org.opendaylight.yangtools.yang.model.api.Status;

public final class RuntimeEnumerationBuilder extends EnumTypeObjectArchetypeBuilder {
    @NonNullByDefault
    public RuntimeEnumerationBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public void setReference(final String reference) {
        // No-op
    }

    @Override
    public void setModuleName(final String moduleName) {
        // No-op
    }

    @Override
    public void setDescription(final String description) {
        // No-op
    }

    @Override
    public void setYangSourceDefinition(final YangSourceDefinition definition) {
        // No-op
    }

    @Override
    RuntimePair createEnumPair(final String name, final String mappedName, final int value, final Status status,
            final String description, final String reference) {
        return new RuntimePair(name, mappedName, value);
    }

    @Override
    @NonNullByDefault
    EnumTypeObjectArchetype build(final List<Pair> values,  final List<AnnotationType> annotations) {
        return new RuntimeEnumTypeObjectArchetype(typeName(), values, annotations);
    }
}
