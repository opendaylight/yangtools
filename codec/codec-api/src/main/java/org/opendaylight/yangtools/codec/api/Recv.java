/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.codec.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * An entity receiving a marshalling stream.
 *
 * @since 16.2.0
 */
@NonNullByDefault
public sealed interface Recv extends Mutable permits Recv.WithInference {
    /**
     * A {@link Recv} with an underlying {@link DataTreeEffectiveStatement}.
     */
    sealed interface OfDataNode extends Recv.WithInference permits RecvAnydata {
        // nothing else
    }

    /**
     * A {@link Recv} with an underlying {@link DataTreeEffectiveStatement}.
     */
    sealed interface WithInference extends Recv permits OfDataNode, ResultRecv {
        /**
         * {@return the underlying statement}
         */
        EffectiveStatementInference inference();
    }
}
