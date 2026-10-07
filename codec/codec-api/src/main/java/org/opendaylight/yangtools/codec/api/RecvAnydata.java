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
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A {@link Recv} receiving {@code anydata} as expressed by an {@link AnydataEffectiveStatement}.
 *
 * @since 16.2.0
 */
@NonNullByDefault
public final class RecvAnydata implements Recv.OfDataNode {
    private final SchemaInferenceStack stack;

    RecvAnydata(final SchemaInferenceStack stack, final AnydataEffectiveStatement stmt) {
        this.stack = requireNonNull(stack);
    }

    @Override
    public EffectiveStatementInference inference() {
        return stack.toInference();
    }
}
