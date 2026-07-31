/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
abstract sealed class ListGenerator extends CompositeSchemaTreeGenerator<ListEffectiveStatement, ListRuntimeType>
        permits EntryObjectGenerator, ItemObjectGenerator {
    @NonNullByDefault
    ListGenerator(final ListEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final StatementNamespace namespace() {
        return StatementNamespace.LIST;
    }

    @Override
    public final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    abstract DataContainerArchetype.OfList createTypeImpl();

    @Override
    final MethodSignature.Builder constructGetter(final DataContainerArchetype.Builder builder, final Type returnType) {
        final var ret = super.constructGetter(builder, returnType).setMechanics(ValueMechanics.NULLIFY_EMPTY);

        builder
            .addMethod(Naming.getNonnullMethodName(localName().getLocalName()))
            .setReturnType(returnType)
            .setDefault(true)
            .addAnnotation(deprecatedAnnotation(statement()));

        return ret;
    }
}
