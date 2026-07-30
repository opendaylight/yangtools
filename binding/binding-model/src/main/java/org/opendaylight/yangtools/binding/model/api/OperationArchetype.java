/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Operation;

/**
 * An {@link Archetype} for a {@link Operation}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface OperationArchetype extends Archetype permits RpcArchetype {
    /**
     * {@return the contract the generated class implements}
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Operation> contract();

    /**
     * {@return the archetype for operation's input}
     */
    InputArchetype input();

    /**
     * {@return the archetype for operation's output}
     */
    OutputArchetype output();
}
