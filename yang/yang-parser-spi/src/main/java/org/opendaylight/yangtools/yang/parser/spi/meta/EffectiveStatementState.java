/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective summary of a {@link EffectiveStatement}'s implementation state. This class serves as a shared concept
 * between {@link StatementFactory} and common reactor, driving statement reuse.
 *
 * <p>
 * {@link StatementFactory} implementations are expected to subclass {@link EffectiveStatementState}, adding whatever
 * additional state is needed and implement {@link #hashCode()} and {@link #equals(Object)} accordingly.
 */
@Beta
public abstract class EffectiveStatementState implements Immutable {
    private final @NonNull Immutable identity;

    protected EffectiveStatementState(final @NonNull Immutable identity) {
        this.identity = requireNonNull(identity);
    }

    protected final @NonNull Immutable identity() {
        return identity;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected @NonNull ToStringHelper addToStringAttributes(final @NonNull ToStringHelper helper) {
        return helper.add("identity", identity);
    }
}
