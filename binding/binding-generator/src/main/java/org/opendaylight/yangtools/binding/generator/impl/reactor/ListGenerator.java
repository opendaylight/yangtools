/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.model.api.ChildOfArchetype;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
@NonNullByDefault
abstract sealed class ListGenerator extends CompositeSchemaTreeGenerator<ListEffectiveStatement, ListRuntimeType>
        permits EntryObjectGenerator, ItemObjectGenerator {
    ListGenerator(final ListEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
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
    abstract ParameterizedType methodReturnType();

    @Override
    final ChildOfArchetype.OfList createTypeImpl(final JavaTypeName typeName, final ListEffectiveStatement statement,
            final List<GroupingArchetype> groupings) {
        return createTypeImpl(typeName, statement, groupings, collectTypeObjects(), collectMethods());
    }

    abstract ChildOfArchetype.OfList createTypeImpl(JavaTypeName typeName, ListEffectiveStatement statement,
        List<GroupingArchetype> groupings, List<TypeObjectArchetype<?>> typeObjects, List<MethodSignature> methods);


    @Override
    final MethodSignature.Builder constructGetter(final List<MethodSignature.Builder> list, final Type returnType) {
        final var localName = localName().getLocalName();
        final var statement = statement();

        // getFoo with nullify
        final var ret = MethodSignature.builder(Naming.getGetterMethodName(localName), returnType,
            ValueMechanics.NULLIFY_EMPTY);
        addDeprecatedAnnotation(ret, statement);
        final var description = statement.descriptionStatement();
        if (description != null) {
            ret.setComment(TypeMemberComment.referenceOf(description.argument()));
        }
        list.add(ret);

        // nonnullFoo
        final var mb = MethodSignature.builderOfDefault(Naming.getNonnullMethodName(localName), returnType,
            ValueMechanics.NORMAL);
        addDeprecatedAnnotation(mb, statement);
        list.add(mb);

        return ret;
    }
}
