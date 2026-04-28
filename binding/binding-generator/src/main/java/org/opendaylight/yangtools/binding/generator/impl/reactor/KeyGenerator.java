/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultKeyRuntimeType;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class KeyGenerator extends AbstractExplicitGenerator<KeyEffectiveStatement, KeyRuntimeType> {
    private final ListGenerator listGen;

    KeyGenerator(final KeyEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent,
            final ListGenerator listGen) {
        super(statement, parent);
        this.listGen = requireNonNull(listGen);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.KEY;
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, listGen.getMember(), Naming.KEY_SUFFIX);
    }

    KeyArchetype getArchetype(final TypeBuilderFactory builderFactory) {
        return (KeyArchetype) getGeneratedType(builderFactory);
    }

    @Override
    KeyArchetype createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newKeyBuilder(typeName(), listGen.typeName(), statement());

        for (var qname : statement().argument()) {
            final var gen = listGen.findSchemaTreeGenerator(qname);
            switch (gen) {
                case null -> throw new VerifyException("Cannot find generator for " + qname);
                case LeafGenerator leafGen -> {
                    builder
                        .addProperty(Naming.getPropertyName(qname.getLocalName()))
                        .setReturnType(leafGen.methodReturnType(builderFactory))
                        .setReadOnly(true);

//                  addComment(propBuilder, leaf);
                }
                default -> throw new VerifyException("Unexpected generator " + gen);
            }
        }

        return builder.build();
    }

    @Override
    KeyRuntimeType createExternalRuntimeType(final Type type) {
        if (type instanceof KeyArchetype archetype) {
            return new DefaultKeyRuntimeType(archetype);
        }
        throw new VerifyException("Unexpected type " + type);
    }

    @Override
    KeyRuntimeType createInternalRuntimeType(final AugmentResolver resolver, final KeyEffectiveStatement statement,
            final Type type) {
        // The only reference to this runtime type is from ListGenerator which is always referencing the external type
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Keys are explicitly handled by their corresponding list
    }
}
