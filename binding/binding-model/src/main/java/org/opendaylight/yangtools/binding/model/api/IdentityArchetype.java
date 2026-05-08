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
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;

/**
 * An {@link Archetype} for a {@link BaseIdentity} generated for an {@link IdentityEffectiveStatement}.
 *
 * @param name this type's {@link JavaTypeName}}
 * @param statement the {@link IdentityEffectiveStatement}
 * @since 16.0.0
 */
@NonNullByDefault
public record IdentityArchetype(
        JavaTypeName name,
        IdentityEffectiveStatement statement,
        List<JavaTypeName> baseIdentities) implements Archetype.WithQName<IdentityEffectiveStatement> {
    private static final List<JavaTypeName> BASE_INTERFACES = List.of(JavaTypeName.create(BaseIdentity.class));

    public IdentityArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        baseIdentities = List.copyOf(baseIdentities);
    }

    /**
     * {@return the non-empty list of interfaces this archetype extends}
     */
    public List<JavaTypeName> interfaces() {
        return baseIdentities.isEmpty() ? BASE_INTERFACES : baseIdentities;
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
        return true;
    }
}
