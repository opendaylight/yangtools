/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Statement-reuse expansion of a single instance. The idea here is that a statement can end up being replicated the
 * same way multiple times -- which does not typically happen, but when it does it is worth exploiting.
 */
final class EffectiveInstances<E extends EffectiveStatement<?, ?>> implements Mutable {
    // Note on sizing: this fits three entries without expansion
    // FIXME: change Immutable to a contract shared with BaseSchemaTreeStatementSupport
    private final Map<Immutable, E> copies = new HashMap<>(4);
    private final @NonNull E local;

    EffectiveInstances(final @NonNull E local) {
        this.local = requireNonNull(local);
        // FIXME: populate copies with 'local' for completeness
    }

    @SuppressWarnings("unchecked")
    static <E extends EffectiveStatement<?, ?>> @NonNull E local(final Object obj) {
        return obj instanceof EffectiveInstances ? ((EffectiveInstances<E>) obj).local : requireNonNull((E) obj);
    }

    @Nullable E lookupCopy(final @NonNull Immutable key) {
        return copies.get(requireNonNull(key));
    }

    void appendCopy(final @NonNull Immutable key, @NonNull final E copy) {
        final E prev = copies.putIfAbsent(requireNonNull(key), requireNonNull(copy));
        verify(prev == null, "Attempted to overwrite %s copy %s with %s", key, prev, copy);
    }
}
