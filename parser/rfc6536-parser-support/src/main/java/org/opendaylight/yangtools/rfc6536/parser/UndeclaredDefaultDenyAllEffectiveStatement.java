/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractUndeclaredEffectiveStatement;

final class UndeclaredDefaultDenyAllEffectiveStatement
        extends AbstractUndeclaredEffectiveStatement<Empty, @NonNull DefaultDenyAllStatement>
        implements DefaultDenyAllEffectiveStatement {
    private static final @NonNull UndeclaredDefaultDenyAllEffectiveStatement EMPTY =
        new UndeclaredDefaultDenyAllEffectiveStatement(ImmutableList.of());

    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements;

    private UndeclaredDefaultDenyAllEffectiveStatement(
            final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements) {
        this.effectiveSubstatements = requireNonNull(effectiveSubstatements);
    }

    static @NonNull UndeclaredDefaultDenyAllEffectiveStatement of(
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements) {
        return effectiveSubstatements.isEmpty() ? EMPTY
            : new UndeclaredDefaultDenyAllEffectiveStatement(effectiveSubstatements);
    }

    @Override
    public Empty argument() {
        return Empty.value();
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return effectiveSubstatements;
    }
}
