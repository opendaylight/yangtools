/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Statement-reuse expansion of a single instance. The idea here is that a statement can end up being replicated the
 * same way multiple times -- which does not typically happen, but when it does it is worth exploiting.
 */
final class EffectiveInstances<E extends EffectiveStatement<?, ?>> implements Mutable {
    private final @NonNull E local;

    EffectiveInstances(final @NonNull E local) {
        this.local = requireNonNull(local);
    }

    @SuppressWarnings("unchecked")
    static <E extends EffectiveStatement<?, ?>> @NonNull E local(final Object obj) {
        return obj instanceof EffectiveInstances ? ((EffectiveInstances<E>) obj).local : requireNonNull((E) obj);
    }
}
