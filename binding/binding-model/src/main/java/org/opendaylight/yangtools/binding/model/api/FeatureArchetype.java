/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * An {@link Archetype} for a {@link YangFeature} generated for a {@link FeatureEffectiveStatement}.
 *
 * @param name this type's {@link JavaTypeName}}
 * @param statement the {@link FeatureEffectiveStatement}
 * @since 16.0.0
 */
@NonNullByDefault
public record FeatureArchetype(
        JavaTypeName name,
        FeatureEffectiveStatement statement) implements Archetype.Compat<FeatureEffectiveStatement> {
    public FeatureArchetype {
        requireNonNull(name);
        requireNonNull(statement);
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<GeneratedProperty> getProperties() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean isAbstract() {
        return false;
    }
}
