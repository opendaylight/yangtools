/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.VerifyException;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A {@link Spliterator} which throws VerifyException on any access. This is used for enforcing single-access to
 *
 */
final class DeadSpliterator implements Spliterator<StmtContext<?, ?, ?>> {
    static final @NonNull Spliterator<StmtContext<?, ?, ?>> INSTANCE = new DeadSpliterator();

    private DeadSpliterator() {
        // Hidden on purpose
    }

    @Override
    public boolean tryAdvance(final Consumer<? super StmtContext<?, ?, ?>> action) {
        throw new VerifyException("Attempted to access effective substatements multiple times");
    }

    @Override
    public Spliterator<StmtContext<?, ?, ?>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | NONNULL;
    }
}
