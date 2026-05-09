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
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * An {@link Archetype} for a {@link Rpc} generated for an {@link RpcEffectiveStatement}.
 *
 * @param name this type's {@link JavaTypeName}}
 * @param statement the {@link RpcEffectiveStatement}
 * @param input this {@link JavaTypeName}} of the corresponding {@link RpcInput} interface
 * @param output this {@link JavaTypeName}} of the corresponding {@link RpcOutput} interface
 * @since 16.0.0
 */
@NonNullByDefault
public record RpcArchetype(
        JavaTypeName name,
        RpcEffectiveStatement statement,
        JavaTypeName input,
        JavaTypeName output) implements Archetype.WithQName<RpcEffectiveStatement> {
    public RpcArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(input);
        requireNonNull(output);
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
