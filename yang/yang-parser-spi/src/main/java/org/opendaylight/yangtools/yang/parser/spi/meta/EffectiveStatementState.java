/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective summary of a {@link EffectiveStatement}'s implementation state. This class serves as a shared concept
 * between {@link StatementFactory} and common reactor, driving statement reuse.
 *
 * <p>
 * {@link StatementFactory} implementations are expected to subclass {@link EffectiveStatementState} or {@link WithPath}
 * and implement {@link #hashCode()} and {@link #equals(Object)}
 */
@Beta
@NonNullByDefault
public abstract class EffectiveStatementState implements Immutable {
    private final Object effectivePath;

    protected EffectiveStatementState(final Object effectivePath) {
        this.effectivePath = effectivePath;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(effectivePath) * 31 + hashCodeImpl();
    }

    protected abstract int hashCodeImpl();

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj
            || obj != null && getClass().equals(obj.getClass()) && equalsImpl((EffectiveStatementState) obj);
    }

    protected abstract boolean equalsImpl(EffectiveStatementState other);

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }
}
