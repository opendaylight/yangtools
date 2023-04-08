/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.UndeclaredStatementFactory;

/**
 * A massively-misnamed superclass for statements which are both schema tree participants and can be created as implicit
 * nodes. This covers {@code case}, {@code input} and {@code output} statements.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
abstract class AbstractImplicitStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends AbstractSchemaTreeStatementSupport<D, E>
        implements UndeclaredStatementFactory<QName, D, E> {
    AbstractImplicitStatementSupport(final StatementDefinition publicDefinition, final StatementPolicy<QName, D> policy,
            final YangParserConfiguration config, final SubstatementValidator validator) {
        super(publicDefinition, policy, config, requireNonNull(validator));
    }

    @Override
    public final E createUndeclaredEffective(final UndeclaredCurrent<QName, D> stmt,
            final @NonNull Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        return createUndeclaredEffective(stmt, buildEffectiveSubstatements(stmt,
            statementsToBuild(stmt, effectiveSubstatements.filter(StmtContext::isSupportedToBuildEffective))));
    }

    abstract @NonNull E createUndeclaredEffective(@NonNull UndeclaredCurrent<QName, D> stmt,
        @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);
}
