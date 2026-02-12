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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;

/**
 * The default implementation {@link DataRootArchetype}.
 * @since 15.0.0
 */
record DataRootArchetypeImpl(
        @NonNull JavaTypeName getIdentifier,
        @NonNull JavaTypeName yangModuleInfo,
        @NonNull List<Type> getImplements,
        @NonNull List<MethodSignature> getMethodDefinitions,
        @NonNull List<GeneratedType> getEnclosedTypes,
        @NonNull List<Enumeration> getEnumerations,
        @Nullable TypeComment getComment,
        @Nullable String getDescription,
        @Nullable String getReference,
        @Nullable String getModuleName) implements DataRootArchetype {
    /**
     * A builder of {@link DataRootArchetype} instances.
     */
    public interface Builder extends GeneratedTypeBuilderBase<Builder> {
        @Override
        DataRootArchetype build();
    }

    DataRootArchetypeImpl {
        requireNonNull(getIdentifier);
        requireNonNull(yangModuleInfo);
        getImplements = List.copyOf(getImplements);
        getMethodDefinitions = List.copyOf(getMethodDefinitions);
        getEnclosedTypes = List.copyOf(getEnclosedTypes);
        getEnumerations = List.copyOf(getEnumerations);
    }
}
