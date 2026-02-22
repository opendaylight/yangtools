/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractPair.RuntimePair;
import org.opendaylight.yangtools.yang.model.api.Status;

public final class RuntimeEnumerationBuilder extends AbstractEnumerationBuilder {
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
    public EnumTypeObjectArchetype build() {
        return new EnumerationImpl(this);
    }

    @Override
    RuntimePair createEnumPair(final String name, final String mappedName, final int value, final Status status,
            final String description, final String reference) {
        return new RuntimePair(name, mappedName, value);
    }

    private static final class EnumerationImpl extends AbstractEnumeration {
        EnumerationImpl(final RuntimeEnumerationBuilder builder) {
            super(builder);
        }

        @Override
        public TypeComment getComment() {
            throw unsupported();
        }

        @Override
        public String getDescription() {
            throw unsupported();
        }

        @Override
        public String getReference() {
            throw unsupported();
        }

        @Override
        public String getModuleName() {
            throw unsupported();
        }

        @Override
        public Optional<YangSourceDefinition> getYangSourceDefinition() {
            throw unsupported();
        }
    }

    static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not available at runtime");
    }
}
