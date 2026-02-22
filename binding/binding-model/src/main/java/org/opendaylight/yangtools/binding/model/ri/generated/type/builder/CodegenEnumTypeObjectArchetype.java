/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;

final class CodegenEnumTypeObjectArchetype extends AbstractEnumTypeObjectArchetype {
    private final String description;
    private final String reference;
    private final String moduleName;
    private final YangSourceDefinition definition;

    @NonNullByDefault
    CodegenEnumTypeObjectArchetype(final JavaTypeName name, final List<Pair> values,
            final List<AnnotationType> annotations, final @Nullable String description,
            final @Nullable String reference, final @Nullable String moduleName,
            final @Nullable YangSourceDefinition definition) {
        super(name, values, annotations);
        this.description = description;
        this.moduleName = moduleName;
        this.reference = reference;
        this.definition = definition;
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