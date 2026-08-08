/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.model.api.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to an {@code input} or an {@code output} statement.
 */
abstract sealed class OperationContainerGenerator<
        S extends DataTreeEffectiveStatement<?>,
        R extends CompositeRuntimeType,
        A extends AugmentableArchetype> extends CompositeSchemaTreeGenerator<S, R>
        permits InputGenerator, OutputGenerator {
    @NonNullByDefault
    OperationContainerGenerator(final S statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final CollisionDomain parentDomain() {
        return getParent().parentDomain();
    }

    @Override
    final DataContainerGenerator<?, ?> getPackageParent() {
        return getParent().getParent();
    }

    @Override
    final Member createMember(final CollisionDomain domain) {
        return createMember(domain, getParent().getMember());
    }

    abstract @NonNull Member createMember(@NonNull CollisionDomain domain, Member parent);

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final A createTypeImpl(final JavaTypeName typeName, final S statement,
            final List<@NonNull GroupingArchetype> groupings) {
        if (getParent() instanceof ActionGenerator actionParent && actionParent.isAddedByUses()) {
            //        final ActionDefinition orig = findOrigAction(parentSchema, action).get();
            //        // Original definition may live in a different module, make sure we account for that
            //        final ModuleContext origContext = moduleContext(
            //            orig.getPath().getPathFromRoot().iterator().next().getModule());
            //        input = context.addAliasType(origContext, orig.getInput(), action.getInput());
            //        output = context.addAliasType(origContext, orig.getOutput(), action.getOutput());
            throw new UnsupportedOperationException("Lookup in original");
        }
        return createArchetype(typeName, statement, groupings, collectTypeObjects(), collectGetters());
    }

    @NonNullByDefault
    abstract @NonNull A createArchetype(JavaTypeName typeName, @NonNull S statement, List<GroupingArchetype> groupings,
        List<TypeObjectArchetype<?>> typeObjects, List<GetterMethod> getters);
}
