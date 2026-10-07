/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.codec.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A {@link Recv} corresponding to a result.
 *
 * @since 16.2.0
 */
@NonNullByDefault
abstract sealed class ResultRecv implements Recv.WithInference permits RecvStructure, RecvYangData {
    final SchemaInferenceStack stack;

    ResultRecv(final SchemaInferenceStack stack) {
        this.stack = requireNonNull(stack);
    }

    @Override
    public final EffectiveStatementInference inference() {
        return stack.toInference();
    }
}
