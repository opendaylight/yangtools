/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultAnydataRuntimeType;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultAnyxmlRuntimeType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.OpaqueObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.runtime.api.AnydataRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.AnyxmlRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.OpaqueRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common generator for {@code anydata} and {@code anyxml}.
 */
abstract class OpaqueObjectGenerator<
        S extends DataTreeEffectiveStatement<?> & DataSchemaCompat<QName, ?>,
        R extends OpaqueRuntimeType> extends AbstractExplicitGenerator<S, R> {
    @NonNullByDefault
    static final class Anydata extends OpaqueObjectGenerator<AnydataEffectiveStatement, AnydataRuntimeType> {
        Anydata(final AnydataEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
            super(statement, parent);
        }

        @Override
        StatementNamespace namespace() {
            return StatementNamespace.ANYDATA;
        }

        @Override
        OpaqueObjectArchetype.Anydata getArchetype(final @Nullable TypeBuilderFactory builderFactory) {
            return (OpaqueObjectArchetype.Anydata) getGeneratedType(builderFactory);
        }

        @Override
        OpaqueObjectArchetype.Anydata createTypeImpl(final JavaTypeName name,
                final AnydataEffectiveStatement statement) {
            return new OpaqueObjectArchetype.Anydata(name, statement);
        }

        @Override
        AnydataRuntimeType createExternalRuntimeType(final Type type) {
            if (!(type instanceof OpaqueObjectArchetype.Anydata archetype)) {
                throw new VerifyException("Unexpected type " + type);
            }
            return new DefaultAnydataRuntimeType(archetype);
        }
    }

    @NonNullByDefault
    static final class Anyxml extends OpaqueObjectGenerator<AnyxmlEffectiveStatement, AnyxmlRuntimeType> {
        Anyxml(final AnyxmlEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
            super(statement, parent);
        }

        @Override
        StatementNamespace namespace() {
            return StatementNamespace.ANYXML;
        }

        @Override
        OpaqueObjectArchetype.Anyxml getArchetype(final @Nullable TypeBuilderFactory builderFactory) {
            return (OpaqueObjectArchetype.Anyxml) getGeneratedType(builderFactory);
        }

        @Override
        OpaqueObjectArchetype.Anyxml createTypeImpl(final JavaTypeName name, final AnyxmlEffectiveStatement statement) {
            return new OpaqueObjectArchetype.Anyxml(name, statement);
        }

        @Override
        AnyxmlRuntimeType createExternalRuntimeType(final Type type) {
            if (!(type instanceof OpaqueObjectArchetype.Anyxml archetype)) {
                throw new VerifyException("Unexpected type " + type);
            }
            return new DefaultAnyxmlRuntimeType(archetype);
        }
    }

    OpaqueObjectGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    abstract @NonNull OpaqueObjectArchetype<S> getArchetype(TypeBuilderFactory builderFactory);

    @Override
    final OpaqueObjectArchetype<S> createTypeImpl(final TypeBuilderFactory builderFactory) {
        return createTypeImpl(typeName(), statement());
    }

    @NonNullByDefault
    abstract OpaqueObjectArchetype<S> createTypeImpl(JavaTypeName name, S statement);

    @Override
    void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructRequireImpl(builder, returnType);
    }

    @Override
    final R createInternalRuntimeType(final AugmentResolver resolver, final S statement, final Type type) {
        return createExternalRuntimeType(type);
    }
}
