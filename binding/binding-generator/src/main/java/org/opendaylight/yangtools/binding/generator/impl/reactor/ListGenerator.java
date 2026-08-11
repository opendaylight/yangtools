/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.model.ChildOfArchetype;
import org.opendaylight.yangtools.binding.model.GetterAnnotation;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
@NonNullByDefault
abstract sealed class ListGenerator extends DataContainerGenerator<ListEffectiveStatement, ListRuntimeType>
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
    final GetterMethod constructGetter(final  ReturnType returnType, final Iterator<GetterAnnotation> annotations) {
        return constructGetter(statement(), returnType, annotations);
    }

    @Override
    final ChildOfArchetype.OfList createTypeImpl(final TypeName typeName, final ListEffectiveStatement statement,
            final List<GroupingArchetype> groupings) {
        return createTypeImpl(typeName, statement, groupings, collectTypeObjects(), collectGetters());
    }

    abstract ChildOfArchetype.OfList createTypeImpl(TypeName typeName, ListEffectiveStatement statement,
        List<GroupingArchetype> groupings, List<TypeObjectArchetype<?>> typeObjects, List<GetterMethod> getters);
}
