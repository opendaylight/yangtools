/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Specialization of {@link BaseStatementSupport} for Boolean statement arguments.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseBooleanStatementSupport<D extends DeclaredStatement<Boolean>,
        E extends EffectiveStatement<Boolean, D>> extends BaseStatementSupport<Boolean, D, E> {
    private final @NonNull E emptyEffectiveFalse;
    private final @NonNull E emptyEffectiveTrue;
    private final @NonNull D emptyDeclaredFalse;
    private final @NonNull D emptyDeclaredTrue;

    protected BaseBooleanStatementSupport(final StatementDefinition publicDefinition,
            final E emptyEffectiveFalse, final E emptyEffectiveTrue) {
        super(publicDefinition);
        this.emptyEffectiveFalse = requireNonNull(emptyEffectiveFalse);
        this.emptyEffectiveTrue = requireNonNull(emptyEffectiveTrue);
        emptyDeclaredFalse = requireNonNull(emptyEffectiveFalse.getDeclared());
        emptyDeclaredTrue = requireNonNull(emptyEffectiveTrue.getDeclared());
    }

    @Override
    public final Boolean parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseBoolean(ctx, value);
    }

    @Override
    public final String internArgument(final String rawArgument) {
        return ArgumentUtils.internBoolean(rawArgument);
    }

    @Override
    protected final D createEmptyDeclared(final StmtContext<Boolean, D, ?> ctx) {
        return ctx.coerceStatementArgument() ? emptyDeclaredTrue : emptyDeclaredFalse;
    }

    @Override
    protected final E createEmptyEffective(final StmtContext<Boolean, D, E> ctx, final D declared) {
        if (emptyDeclaredTrue.equals(declared)) {
            return emptyEffectiveTrue;
        } else if (emptyDeclaredFalse.equals(declared)) {
            return emptyEffectiveFalse;
        } else {
            return createEmptyEffective(declared);
        }
    }

    protected abstract @NonNull E createEmptyEffective(@NonNull D declared);
}
