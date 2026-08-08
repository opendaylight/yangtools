/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.OperationArchetype;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.OperationRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public abstract sealed class OperationGenerator<
        S extends SchemaTreeEffectiveStatement<?>,
        R extends OperationRuntimeType> extends CompositeSchemaTreeGenerator<S, R>
        permits AbstractActionGenerator, RpcGenerator {
    @NonNullByDefault
    OperationGenerator(final S statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final void addAsGetterMethod(final List<GetterMethod.Builder> list) {
        // RPCs/Actions are a separate concept
    }

    @Override
    final OperationArchetype createTypeImpl(final TypeName typeName, final S statement,
            final List<GroupingArchetype> groupings) {
        if (!groupings.isEmpty()) {
            throw new VerifyException("Illegal grouping in " + statement);
        }
        return createTypeImpl(typeName, statement,
            getContainer(RpcInputArchetype.class, InputGenerator.class),
            getContainer(RpcOutputArchetype.class, OutputGenerator.class));
    }

    @NonNullByDefault
    abstract OperationArchetype createTypeImpl(TypeName typeName, @NonNull S statement, RpcInputArchetype input,
        RpcOutputArchetype output);

    @NonNullByDefault
    private <A extends AugmentableArchetype, G extends OperationContainerGenerator<?, ?, ?>> A getContainer(
            final Class<A> archetypeClass, final Class<G> generatorClass) {
        for (var child : this) {
            if (generatorClass.isInstance(child)) {
                final var original = generatorClass.cast(child).getOriginal().getGeneratedType();
                if (archetypeClass.isInstance(original)) {
                    return archetypeClass.cast(original);
                }
                throw new VerifyException("Unexpected archetype " + original);
            }
        }
        throw new VerifyException("No " + generatorClass.getSimpleName() + " in " + this);
    }
}
