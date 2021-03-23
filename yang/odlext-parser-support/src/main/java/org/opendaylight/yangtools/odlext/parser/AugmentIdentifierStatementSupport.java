/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierStatement;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class AugmentIdentifierStatementSupport
        extends AbstractStatementSupport<UnqualifiedQName, AugmentIdentifierStatement,
                                         AugmentIdentifierEffectiveStatement> {
    public static final @NonNull AugmentIdentifierStatementSupport INSTANCE = new AugmentIdentifierStatementSupport();

    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenDaylightExtensionsStatements.AUGMENT_IDENTIFIER).build();

    private AugmentIdentifierStatementSupport() {
        super(OpenDaylightExtensionsStatements.AUGMENT_IDENTIFIER, StatementPolicy.contextIndependent());
    }

    @Override
    public UnqualifiedQName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return UnqualifiedQName.of(value).intern();
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid identifier \"%s\"", value);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected AugmentIdentifierStatement createDeclared(
            final StmtContext<UnqualifiedQName, AugmentIdentifierStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new AugmentIdentifierStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected AugmentIdentifierEffectiveStatement createEffective(
            final Current<UnqualifiedQName, AugmentIdentifierStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AugmentIdentifierEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
