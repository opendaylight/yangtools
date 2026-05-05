/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
        @Nullable YangSourceDefinition yangSourceDefinition,
        @Nullable TypeComment getComment) implements FeatureArchetype {
    RuntimeFeatureArchetype {
        requireNonNull(name);
        requireNonNull(dataRoot);
        requireNonNull(statement);
    }
}
