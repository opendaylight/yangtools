/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractPair.CodegenPair;
import org.opendaylight.yangtools.yang.model.api.Status;

public final class CodegenEnumerationBuilder extends EnumTypeObjectArchetypeBuilder {
    private String description;
    private String reference;
    private String moduleName;
    private YangSourceDefinition definition;

    @NonNullByDefault
    public CodegenEnumerationBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setYangSourceDefinition(final YangSourceDefinition yangSourceDefinition) {
        definition = yangSourceDefinition;
    }

    @Override
    CodegenPair createEnumPair(final String name, final String mappedName, final int value, final Status status,
            final String enumDescription, final String enumReference) {
        return new CodegenPair(name, mappedName, value, status, enumDescription, enumReference);
    }

    @Override
    @NonNullByDefault
    EnumTypeObjectArchetype build(final List<Pair> values,  final List<AnnotationType> annotations) {
        return new CodegenEnumTypeObjectArchetype(typeName(), values, annotations, description, reference, moduleName,
            definition);
    }
}
