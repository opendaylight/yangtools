/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link RpcOutput} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface RpcOutputArchetype extends AugmentableArchetype permits RpcOutputArchetypeImpl {
    @Override
    OutputEffectiveStatement statement();

    static RpcOutputArchetype of(final JavaTypeName typeName, final OutputEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<MethodSignature> methods) {
        return new RpcOutputArchetypeImpl(typeName, statement, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(methods));
    }
}
