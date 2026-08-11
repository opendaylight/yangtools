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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultContainerRuntimeType;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.GetterAnnotation;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code container} statement.
 */
final class ContainerGenerator extends AugmentableGenerator<ContainerEffectiveStatement, ContainerRuntimeType> {
    @NonNullByDefault
    ContainerGenerator(final ContainerEffectiveStatement statement, final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.CONTAINER;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    ContainerObjectArchetype methodReturnType() {
        return (ContainerObjectArchetype) getGeneratedType();
    }

    @Override
    GetterMethod constructGetter(final ReturnType returnType, final Iterator<@NonNull GetterAnnotation> annotations) {
        return constructGetter(statement(), returnType, annotations);
    }

    @Override
    ContainerObjectArchetype createTypeImpl(final TypeName typeName, final ContainerEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings) {
        return ContainerObjectArchetype.of(typeName, statement, parentNameForChildOf(), groupings, collectTypeObjects(),
            collectGetters());
    }

    @Override
    CompositeRuntimeTypeBuilder<ContainerEffectiveStatement, ContainerRuntimeType> createBuilder(
            final ContainerEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ContainerRuntimeType build(final Archetype type, final ContainerEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultContainerRuntimeType((ContainerObjectArchetype) type, statement, children, augments);
            }
        };
    }
}
