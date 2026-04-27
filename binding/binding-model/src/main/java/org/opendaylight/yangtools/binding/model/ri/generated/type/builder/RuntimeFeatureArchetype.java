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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * A run-time implementation of {@link FeatureArchetype}.
 */
record RuntimeFeatureArchetype(
        @NonNull JavaTypeName name,
        @NonNull JavaTypeName dataRoot,
        @NonNull FeatureEffectiveStatement statement,
        @NonNull List<AnnotationType> getAnnotations,
        @NonNull List<Constant> getConstantDefinitions,
        @Nullable YangSourceDefinition yangSourceDefinition,
        @Nullable TypeComment getComment) implements FeatureArchetype {
    RuntimeFeatureArchetype {
        requireNonNull(name);
        requireNonNull(dataRoot);
        requireNonNull(statement);
        getAnnotations = List.copyOf(getAnnotations);
        getConstantDefinitions = List.copyOf(getConstantDefinitions);
    }

    @Override
    public Optional<YangSourceDefinition> getYangSourceDefinition() {
        return Optional.ofNullable(yangSourceDefinition);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReference() {
        return null;
    }

    @Override
    public String getModuleName() {
        return null;
    }
}
