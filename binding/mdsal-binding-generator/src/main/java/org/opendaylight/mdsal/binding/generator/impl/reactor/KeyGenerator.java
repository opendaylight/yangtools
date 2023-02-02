/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultKeyRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
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
        return domain.addSecondary(this, listGen.getMember(), BindingMapping.KEY_SUFFIX);
    }

    @Override
    GeneratedTransferObject createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTOBuilder(typeName());

        builder.addImplementsType(BindingTypes.identifier(Type.of(listGen.typeName())));

        final var leafNames = statement().argument();
        for (var listChild : listGen) {
            if (listChild instanceof LeafGenerator leafGen) {
                final QName qname = leafGen.statement().argument();
                if (leafNames.contains(qname)) {
                    final var prop = builder
                        .addProperty(BindingMapping.getPropertyName(qname.getLocalName()))
                        .setReturnType(leafGen.methodReturnType(builderFactory))
                        .setReadOnly(true);

//                    addComment(propBuilder, leaf);

                    builder.addEqualsIdentity(prop);
                    builder.addHashIdentity(prop);
                    builder.addToStringProperty(prop);
                }
            }
        }

        // serialVersionUID
        addSerialVersionUID(builder);

        return builder.build();
    }

    @Override
    KeyRuntimeType createExternalRuntimeType(final Type type) {
        verify(type instanceof GeneratedTransferObject, "Unexpected type %s", type);
        return new DefaultKeyRuntimeType((GeneratedTransferObject) type, statement());
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
