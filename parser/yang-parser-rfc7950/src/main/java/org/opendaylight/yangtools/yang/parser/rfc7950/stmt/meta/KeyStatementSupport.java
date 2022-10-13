/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.antlr.GrammarUtils;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class KeyStatementSupport
        extends AbstractStatementSupport<Set<QName>, KeyStatement, KeyEffectiveStatement> {

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.KEY).build();

    public KeyStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.KEY, StatementPolicy.copyDeclared(
            // Identity comparison is sufficient because adaptArgumentValue() is careful about reuse.
            (copy, current, substatements) -> copy.getArgument() == current.getArgument()),
            config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public ImmutableSet<QName> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final var builder = ImmutableSet.<QName>builder();
        int tokens = 0;
        for (var keyToken : GrammarUtils.splitKeyArg(value)) {
            builder.add(StmtContextUtils.parseNodeIdentifier(ctx, keyToken));
            tokens++;
        }

        // Throws NPE on nulls, retains first inserted value, cannot be modified
        final var ret = builder.build();
        SourceException.throwIf(ret.size() != tokens, ctx, "Key argument '%s' contains duplicates", value);
        return ret;
    }

    @Override
    public Set<QName> adaptArgumentValue(final StmtContext<Set<QName>, KeyStatement, KeyEffectiveStatement> ctx,
            final QNameModule targetModule) {
        final var builder = ImmutableSet.<QName>builder();
        boolean replaced = false;
        for (var qname : ctx.getArgument()) {
            if (!targetModule.equals(qname.getModule())) {
                builder.add(qname.bindTo(targetModule).intern());
                replaced = true;
            } else {
                builder.add(qname);
            }
        }

        // This makes sure we reuse the collection when a grouping is instantiated in the same module.
        return replaced ? builder.build() : ctx.argument();
    }

    @Override
    protected KeyStatement createDeclared(final BoundStmtCtx<Set<QName>> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createKey(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected KeyStatement attachDeclarationReference(final KeyStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateKey(stmt, reference);
    }

    @Override
    protected KeyEffectiveStatement createEffective(final Current<Set<QName>, KeyStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createKey(stmt.declared(), stmt.getArgument(), substatements);
    }
}
