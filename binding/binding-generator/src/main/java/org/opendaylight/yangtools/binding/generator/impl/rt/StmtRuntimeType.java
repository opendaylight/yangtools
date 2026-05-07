/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An extension to {@link AbstractRuntimeType} that stores an explicit statement.
 */
abstract class StmtRuntimeType<S extends EffectiveStatement<?, ?>, T extends Type> extends AbstractRuntimeType<S, T> {
    private final @NonNull S statement;

    @NonNullByDefault
    StmtRuntimeType(final T bindingType, final S statement) {
        super(bindingType);
        this.statement = requireNonNull(statement);
    }

    @Override
    public final S statement() {
        return statement;
    }
}
