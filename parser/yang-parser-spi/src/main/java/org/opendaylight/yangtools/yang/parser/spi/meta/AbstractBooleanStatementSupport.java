/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Specialization of {@link AbstractStatementSupport} for statements which carry a Boolean argument and are essentially
 * context-independent.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class AbstractBooleanStatementSupport<D extends DeclaredStatement<Boolean>,
        E extends EffectiveStatement<Boolean, D>> extends AbstractStatementSupport<Boolean, D, E> {
    private final @NonNull E emptyEffectiveFalse;
    private final @NonNull E emptyEffectiveTrue;
    private final @NonNull D emptyDeclaredFalse;
    private final @NonNull D emptyDeclaredTrue;

    protected AbstractBooleanStatementSupport(final StatementDefinition publicDefinition,
            final E emptyEffectiveFalse, final E emptyEffectiveTrue, final StatementPolicy<Boolean, D> policy,
            final YangParserConfiguration config, final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
        this.emptyEffectiveFalse = requireNonNull(emptyEffectiveFalse);
        this.emptyEffectiveTrue = requireNonNull(emptyEffectiveTrue);
        emptyDeclaredFalse = verifyNotNull(emptyEffectiveFalse.getDeclared());
        emptyDeclaredTrue = verifyNotNull(emptyEffectiveTrue.getDeclared());
    }

    @Override
    public final Boolean parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return switch (value) {
            case "false" -> Boolean.FALSE;
            case "true" -> Boolean.TRUE;
            case null, default -> throw new SourceException(ctx,
                "Invalid '%s' statement %s '%s', it can be either 'true' or 'false'", statementName(), argumentName(),
                value);
        };
    }

    @Override
    public final String internArgument(final String rawArgument) {
        return switch (rawArgument) {
            case "false" -> "false";
            case "true" -> "true";
            case null, default -> rawArgument;
        };
    }

    @Override
    protected final D createDeclared(final BoundStmtCtx<Boolean> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        final var argument = ctx.getArgument();
        if (substatements.isEmpty()) {
            return argument ? emptyDeclaredTrue : emptyDeclaredFalse;
        }
        return createDeclared(argument, substatements);
    }

    protected abstract @NonNull D createDeclared(@NonNull Boolean argument,
        @NonNull ImmutableList<DeclaredStatement<?>> substatements);

    @Override
    protected final E createEffective(final Current<Boolean, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createEmptyEffective(stmt) : createEffective(stmt.declared(), substatements);
    }

    protected abstract @NonNull E createEffective(@NonNull D declared,
        ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    protected abstract @NonNull E createEmptyEffective(@NonNull D declared);

    private @NonNull E createEmptyEffective(final Current<Boolean, D> stmt) {
        final var declared = stmt.declared();
        if (emptyDeclaredTrue.equals(declared)) {
            return emptyEffectiveTrue;
        }
        if (emptyDeclaredFalse.equals(declared)) {
            return emptyEffectiveFalse;
        }
        return createEmptyEffective(declared);
    }
}
