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
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultContainerRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code container} statement.
 */
final class ContainerGenerator extends CompositeSchemaTreeGenerator<ContainerEffectiveStatement, ContainerRuntimeType> {
    @NonNullByDefault
    ContainerGenerator(final ContainerEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
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
    ContainerObjectArchetype createTypeImpl(final JavaTypeName typeName, final ContainerEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings) {
        return ContainerObjectArchetype.of(typeName, statement, parentNameForChildOf(), groupings,
            collectEnclosedTypes(), collectGetterMethods());
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

    @Override
    MethodSignature.Builder constructGetter(final List<MethodSignature.@NonNull Builder> list, final Type returnType) {
        final var ret = super.constructGetter(list, returnType).setMechanics(ValueMechanics.NORMAL);
        final var statement = statement();
        if (statement.presenceStatement() == null) {
            final var mb = MethodSignature.builder(Naming.getNonnullMethodName(localName().getLocalName()))
                .setReturnType(returnType)
                .setDefault(false);
            addDeprecatedAnnotation(mb, statement);
            list.add(mb);
        }
        return ret;
    }
}
