/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;

/**
 * An {@link Archetype} for a {@link BaseIdentity} specialization generated for an {@link IdentityEffectiveStatement}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface IdentityArchetype extends Archetype.WithQName<IdentityEffectiveStatement> {
    /**
     * An {@link IdentityArchetype} specialized from {@link BaseIdentity}.
     *
     * @param name this type's {@link JavaTypeName}}
     * @param statement the {@link IdentityEffectiveStatement}
     * @since 16.0.0
     */
    record Base(JavaTypeName name, IdentityEffectiveStatement statement) implements IdentityArchetype {
        private static final List<JavaTypeName> INTERFACES = List.of(JavaTypeName.create(BaseIdentity.class));

        public Base {
            requireNonNull(name);
            requireNonNull(statement);
        }

        @Override
        public List<JavaTypeName> interfaces() {
            return INTERFACES;
        }
    }

    /**
     * An {@link IdentityArchetype} specialized from one or more generated interfaces.
     *
     * @param name this type's {@link JavaTypeName}}
     * @param statement the {@link IdentityEffectiveStatement}
     * @param interfaces specialized interfaces
     * @since 16.0.0
     */
    record Derived(
            JavaTypeName name,
            IdentityEffectiveStatement statement,
            List<JavaTypeName> interfaces) implements IdentityArchetype {
        public Derived {
            requireNonNull(name);
            requireNonNull(statement);
            interfaces = List.copyOf(interfaces);
            checkArgument(!interfaces.isEmpty());
        }
    }

    /**
     * {@return the non-empty list of interfaces this archetype extends}
     */
    List<JavaTypeName> interfaces();

    @Override
    @Deprecated(forRemoval = true)
    default List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default boolean isAbstract() {
        return true;
    }
}
