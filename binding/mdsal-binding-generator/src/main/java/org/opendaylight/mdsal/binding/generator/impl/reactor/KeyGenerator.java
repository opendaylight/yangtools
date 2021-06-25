/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class KeyGenerator extends AbstractExplicitGenerator<KeyEffectiveStatement> {
    // FIXME: this should be a well-known constant
    private static final String SUFFIX = "Key";

    private final ListGenerator listGen;

    KeyGenerator(final KeyEffectiveStatement statement, final AbstractCompositeGenerator<?> parent,
            final ListGenerator listGen) {
        super(statement, parent);
        this.listGen = requireNonNull(listGen);
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, listGen.getMember(), SUFFIX);
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());

        builder.addImplementsType(BindingTypes.identifier(Type.of(listGen.typeName())));

        final Set<QName> leafNames = statement().argument();
        for (Generator listChild : listGen) {
            if (listChild instanceof LeafGenerator) {
                final LeafGenerator leafGen = (LeafGenerator) listChild;
                final QName qname = leafGen.statement().argument();
                if (leafNames.contains(qname)) {
                    final GeneratedPropertyBuilder prop = builder
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
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Keys are explicitly handled by their corresponding list
    }
}
