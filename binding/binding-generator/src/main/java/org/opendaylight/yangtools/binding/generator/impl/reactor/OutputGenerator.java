/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultOutputRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * Generator corresponding to an {@code output} statement.
 */
final class OutputGenerator
        extends OperationContainerGenerator<OutputEffectiveStatement, OutputRuntimeType, RpcOutputArchetype> {
    @NonNullByDefault
    OutputGenerator(final OutputEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.OUTPUT;
    }

    @Override
    Member createMember(final CollisionDomain domain, final Member parent) {
        return domain.addSecondary(this, parent);
    }

    @Override
    RpcOutputArchetype createArchetype(final JavaTypeName typeName, final OutputEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings, final List<@NonNull TypeObjectArchetype<?>> typeObjects,
            final List<@NonNull GetterMethod> methods) {
        return RpcOutputArchetype.of(typeName, statement, groupings, typeObjects, methods);
    }

    @Override
    CompositeRuntimeTypeBuilder<OutputEffectiveStatement, OutputRuntimeType> createBuilder(
            final OutputEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            OutputRuntimeType build(final Archetype type, final OutputEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultOutputRuntimeType((RpcOutputArchetype) type, statement, children, augments);
            }
        };
    }
}
