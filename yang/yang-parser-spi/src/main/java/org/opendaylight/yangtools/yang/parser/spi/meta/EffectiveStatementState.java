/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective summary of a {@link EffectiveStatement}'s implementation state. This class serves as a shared concept
 * between {@link StatementFactory} and common reactor, driving statement reuse.
 *
 * <p>
 * {@link StatementFactory} implementations are expected to subclass {@link EffectiveStatementState}, adding whatever
 * additional state is needed and implement {@link #hashCodeImpl()} and {@link #equalsImpl(EffectiveStatementState)}
 * accordingly.
 */
@Beta
public abstract class EffectiveStatementState implements Immutable {
    private final @NonNull Immutable identity;

    protected EffectiveStatementState(final @NonNull Immutable identity) {
        this.identity = requireNonNull(identity);
    }

    @Override
    public final int hashCode() {
        return identity.hashCode() * 31 + hashCodeImpl();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        final EffectiveStatementState other = (EffectiveStatementState) obj;
        return identity.equals(other.identity) && equalsImpl(other);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected @NonNull ToStringHelper addToStringAttributes(final @NonNull ToStringHelper helper) {
        return helper.add("identity", identity);
    }

    protected abstract int hashCodeImpl();

    protected abstract boolean equalsImpl(EffectiveStatementState other);
}
