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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * Builder for {@link DataRootArchetype}.
 */
public final class DataRootArchetypeBuilder extends AbstractGeneratedTypeBuilder<DataRootArchetype.Builder>
        implements DataRootArchetype.Builder {
    private final @NonNull ModuleEffectiveStatement statement;

    @NonNullByDefault
    public DataRootArchetypeBuilder(final JavaTypeName typeName, final ModuleEffectiveStatement statement) {
        super(typeName);
        this.statement = requireNonNull(statement);
        addImplementsType(BindingTypes.dataRoot(TypeRef.of(typeName)));
    }

    @Override
    @Deprecated(forRemoval = true)
    public void setModuleName(final String moduleName) {
        // no-op
    }

    @Override
    @Deprecated(forRemoval = true)
    public void setDescription(final String description) {
        // No-op
    }

    @Override
    @Deprecated(forRemoval = true)
    public void setReference(final String reference) {
        // No-op
    }

    @Override
    public DataRootArchetype build() {
        return new DataRootArchetypeImpl(typeName(), statement, getImplementsTypes(),
            AbstractGeneratedType.toUnmodifiableMethods(getMethodDefinitions()),
            List.copyOf(getEnclosedTypes()));
    }

    @Override
    protected DataRootArchetype.Builder thisInstance() {
        return this;
    }
}
