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
import org.opendaylight.yangtools.odlext.model.api.AugmentEquivalentEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AugmentEquivalentStatement;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class AugmentEquivalentStatementSupport
        extends AbstractStatementSupport<QName, AugmentEquivalentStatement, AugmentEquivalentEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenDaylightExtensionsStatements.AUGMENT_EQUIVALENT).build();

    public AugmentEquivalentStatementSupport(final YangParserConfiguration config) {
        super(OpenDaylightExtensionsStatements.AUGMENT_EQUIVALENT, StatementPolicy.exactReplica(), config, VALIDATOR);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    protected AugmentEquivalentStatement createDeclared(final StmtContext<QName, AugmentEquivalentStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new AugmentEquivalentStatementImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected AugmentEquivalentStatement attachDeclarationReference(final AugmentEquivalentStatement stmt,
            final DeclarationReference reference) {
        return new RefAugmentEquivalentStatement(stmt, reference);
    }

    @Override
    protected AugmentEquivalentEffectiveStatement createEffective(final Current<QName, AugmentEquivalentStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AugmentEquivalentEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
