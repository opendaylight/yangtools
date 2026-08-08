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
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultInputRuntimeType;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.InputRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * Generator corresponding to an {@code input} statement. We use a combination of the operation name and "Input"
 * as the name. This makes it easier to support multiple RPCs/actions in one source file, as we can import them without
 * a conflict.
 */
final class InputGenerator
        extends OperationContainerGenerator<InputEffectiveStatement, InputRuntimeType, RpcInputArchetype> {
    @NonNullByDefault
    InputGenerator(final InputEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.INPUT;
    }

    @Override
    Member createMember(final CollisionDomain domain, final Member parent) {
        return domain.addSecondary(this, parent);
    }

    @Override
    RpcInputArchetype createArchetype(final TypeName typeName, final InputEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings, final List<@NonNull TypeObjectArchetype<?>> typeObjects,
            final List<@NonNull GetterMethod> getters) {
        return RpcInputArchetype.of(typeName, statement, groupings, typeObjects, getters);
    }

    @Override
    CompositeRuntimeTypeBuilder<InputEffectiveStatement, InputRuntimeType> createBuilder(
            final InputEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            InputRuntimeType build(final Archetype type, final InputEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultInputRuntimeType((RpcInputArchetype) type, statement, children, augments);
            }
        };
    }
}
