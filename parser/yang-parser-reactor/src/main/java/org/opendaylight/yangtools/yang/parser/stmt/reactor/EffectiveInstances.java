/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statement-reuse expansion of a single instance. The idea here is that a statement can end up being replicated the
 * same way multiple times -- which does not typically happen, but when it does it is worth exploiting.
 *
 * @param <E> {@link EffectiveStatement} type
 */
final class EffectiveInstances<E extends EffectiveStatement<?, ?>> implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(EffectiveInstances.class);

    // Note on sizing: this fits three entries without expansion. Note we do not include the local copy, as it does
    // not have the same original.
    private final Map<EffectiveStatementState, E> copies = new HashMap<>(4);
    private final @NonNull E local;

    EffectiveInstances(final E local) {
        this.local = requireNonNull(local);
    }

    @SuppressWarnings("unchecked")
    static <E extends EffectiveStatement<?, ?>> @NonNull E local(final Object obj) {
        return obj instanceof EffectiveInstances ? ((EffectiveInstances<E>) obj).local : requireNonNull((E) obj);
    }

    @NonNull E attachCopy(final @NonNull EffectiveStatementState key, @NonNull final E copy) {
        final E prev = copies.putIfAbsent(requireNonNull(key), requireNonNull(copy));
        if (prev == null) {
            // Nothing matching state
            return copy;
        }

        // We need to make sure substatements are actually the same. If they are not, we'll just return the copy,
        // playing it safe.
        final var prevStmts = prev.effectiveSubstatements();
        final var copyStmts = copy.effectiveSubstatements();
        if (prevStmts != copyStmts) {
            if (prevStmts.size() != copyStmts.size()) {
                LOG.trace("Key {} substatement count mismatch", key);
                return copy;
            }

            final var prevIt = prevStmts.iterator();
            final var copyIt = copyStmts.iterator();
            while (prevIt.hasNext()) {
                if (prevIt.next() != copyIt.next()) {
                    LOG.trace("Key {} substatement mismatch", key);
                    return copy;
                }
            }
        }

        LOG.trace("Key {} substatement reused", key);
        return prev;
    }
}
