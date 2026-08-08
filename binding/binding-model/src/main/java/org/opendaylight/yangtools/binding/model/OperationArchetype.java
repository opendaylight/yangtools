/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Operation;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * An {@link Archetype} for a {@link Operation}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface OperationArchetype extends Archetype permits OperationArchetype.OfAction, RpcArchetype {
    /**
     * An {@link OperationArchetype} for a {@code action} statement.
     */
    sealed interface OfAction extends OperationArchetype permits ActionArchetype, KeyedListActionArchetype {
        @Override
        ActionEffectiveStatement statement();

        /**
         * {@return the {@link TypeName} of the archetype in which this action is defined}
         */
        TypeName parentName();
    }

    /**
     * {@return the contract the generated class implements}
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Operation> contract();

    /**
     * {@return the archetype for operation's input}
     */
    RpcInputArchetype input();

    /**
     * {@return the archetype for operation's output}
     */
    RpcOutputArchetype output();
}
