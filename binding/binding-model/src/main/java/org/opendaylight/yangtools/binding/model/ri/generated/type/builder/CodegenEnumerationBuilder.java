/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
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
    public EnumTypeObjectArchetype build() {
        return new EnumerationImpl(this);
    }

    @Override
    CodegenPair createEnumPair(final String name, final String mappedName, final int value, final Status status,
            final String enumDescription, final String enumReference) {
        return new CodegenPair(name, mappedName, value, status, enumDescription, enumReference);
    }

    private static final class EnumerationImpl extends AbstractEnumeration {
        private final String description;
        private final String reference;
        private final String moduleName;
        private final YangSourceDefinition definition;

        EnumerationImpl(final CodegenEnumerationBuilder builder) {
            super(builder);
            description = builder.description;
            moduleName = builder.moduleName;
            reference = builder.reference;
            definition = builder.definition;
        }

        @Override
        public TypeComment getComment() {
            return null;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public String getModuleName() {
            return moduleName;
        }

        @Override
        public Optional<YangSourceDefinition> getYangSourceDefinition() {
            return Optional.ofNullable(definition);
        }
    }
}
