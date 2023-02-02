/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultAnydataRuntimeType;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultAnyxmlRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AnydataRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AnyxmlRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OpaqueRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common generator for {@code anydata} and {@code anyxml}.
 */
abstract class OpaqueObjectGenerator<S extends DataTreeEffectiveStatement<?>, R extends OpaqueRuntimeType>
        extends AbstractExplicitGenerator<S, R> {
    static final class Anydata extends OpaqueObjectGenerator<AnydataEffectiveStatement, AnydataRuntimeType> {
        Anydata(final AnydataEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
            super(statement, parent);
        }

        @Override
        StatementNamespace namespace() {
            return StatementNamespace.ANYDATA;
        }

        @Override
        AnydataRuntimeType createExternalRuntimeType(final GeneratedType type) {
            return new DefaultAnydataRuntimeType(type, statement());
        }

        @Override
        AnydataRuntimeType createInternalRuntimeType(final AnydataEffectiveStatement statement,
                final GeneratedType type) {
            return new DefaultAnydataRuntimeType(type, statement);
        }
    }

    static final class Anyxml extends OpaqueObjectGenerator<AnyxmlEffectiveStatement, AnyxmlRuntimeType> {
        Anyxml(final AnyxmlEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
            super(statement, parent);
        }

        @Override
        StatementNamespace namespace() {
            return StatementNamespace.ANYXML;
        }

        @Override
        AnyxmlRuntimeType createExternalRuntimeType(final GeneratedType type) {
            return new DefaultAnyxmlRuntimeType(type, statement());
        }

        @Override
        AnyxmlRuntimeType createInternalRuntimeType(final AnyxmlEffectiveStatement statement,
                final GeneratedType type) {
            return new DefaultAnyxmlRuntimeType(type, statement);
        }
    }

    OpaqueObjectGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.opaqueObject(builder));
        addImplementsChildOf(builder);
        defaultImplementedInterace(builder);
        annotateDeprecatedIfNecessary(builder);

        final var module = currentModule();
        module.addQNameConstant(builder, localName());

        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());
//        newType.setSchemaPath(schemaNode.getPath());

        return builder.build();
    }

    @Override
    void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructRequireImpl(builder, returnType);
    }

    @Override
    final @NonNull R createExternalRuntimeType(final Type type) {
        verify(type instanceof GeneratedType, "Unexpected type %s", type);
        return createExternalRuntimeType((GeneratedType) type);
    }

    abstract @NonNull R createExternalRuntimeType(@NonNull GeneratedType type);

    @Override
    final R createInternalRuntimeType(final AugmentResolver resolver, final S statement, final Type type) {
        verify(type instanceof GeneratedType, "Unexpected type %s", type);
        return createInternalRuntimeType(statement, (GeneratedType) type);
    }

    abstract @NonNull R createInternalRuntimeType(@NonNull S statement, @NonNull GeneratedType type);
}
