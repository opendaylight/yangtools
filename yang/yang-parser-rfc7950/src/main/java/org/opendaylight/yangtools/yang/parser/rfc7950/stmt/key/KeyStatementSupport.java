/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class KeyStatementSupport
        extends BaseStatementSupport<Collection<SchemaNodeIdentifier>, KeyStatement, KeyEffectiveStatement> {
    private static final Splitter LIST_KEY_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.KEY)
        .build();
    private static final KeyStatementSupport INSTANCE = new KeyStatementSupport();

    private KeyStatementSupport() {
        super(YangStmtMapping.KEY);
    }

    public static KeyStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<SchemaNodeIdentifier> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final Builder<SchemaNodeIdentifier> builder = ImmutableSet.builder();
        int tokens = 0;
        for (String keyToken : LIST_KEY_SPLITTER.split(value)) {
            builder.add(SchemaNodeIdentifier.SAME.createChild(StmtContextUtils.parseNodeIdentifier(ctx, keyToken)));
            tokens++;
        }

        // Throws NPE on nulls, retains first inserted value, cannot be modified
        final Collection<SchemaNodeIdentifier> ret = builder.build();
        SourceException.throwIf(ret.size() != tokens, ctx.getStatementSourceReference(),
                "Key argument '%s' contains duplicates", value);
        return ret;
    }

    @Override
    public Collection<SchemaNodeIdentifier> adaptArgumentValue(
            final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, KeyEffectiveStatement> ctx,
            final QNameModule targetModule) {
        final Builder<SchemaNodeIdentifier> builder = ImmutableSet.builder();
        boolean replaced = false;
        for (final SchemaNodeIdentifier arg : ctx.coerceStatementArgument()) {
            final QName qname = arg.getLastComponent();
            if (!targetModule.equals(qname.getModule())) {
                final QName newQname = qname.withModule(targetModule).intern();
                builder.add(SchemaNodeIdentifier.SAME.createChild(newQname));
                replaced = true;
            } else {
                builder.add(arg);
            }
        }

        // This makes sure we reuse the collection when a grouping is
        // instantiated in the same module
        return replaced ? builder.build() : ctx.getStatementArgument();
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected KeyStatement createDeclared(final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularKeyStatement(ctx, substatements);
    }

    @Override
    protected KeyStatement createEmptyDeclared(
            final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> ctx) {
        return new EmptyKeyStatement(ctx);
    }

    @Override
    protected KeyEffectiveStatement createEffective(
            final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, KeyEffectiveStatement> ctx,
            final KeyStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final Collection<SchemaNodeIdentifier> arg = ctx.coerceStatementArgument();
        return arg.equals(declared.argument()) ? new RegularLocalKeyEffectiveStatement(declared, substatements)
                : new RegularForeignKeyEffectiveStatement(declared, arg, substatements);
    }

    @Override
    protected KeyEffectiveStatement createEmptyEffective(
            final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, KeyEffectiveStatement> ctx,
            final KeyStatement declared) {
        final Collection<SchemaNodeIdentifier> arg = ctx.coerceStatementArgument();
        return arg.equals(declared.argument()) ? new EmptyLocalKeyEffectiveStatement(declared)
                : new EmptyForeignKeyEffectiveStatement(declared, arg);
    }
}
