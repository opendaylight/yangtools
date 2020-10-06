/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import static com.google.common.base.Verify.verify;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class KeyStatementSupport
        extends BaseStatementSupport<Set<QName>, KeyStatement, KeyEffectiveStatement> {
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
    public ImmutableSet<QName> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final Builder<QName> builder = ImmutableSet.builder();
        int tokens = 0;
        for (String keyToken : LIST_KEY_SPLITTER.split(value)) {
            builder.add(StmtContextUtils.parseNodeIdentifier(ctx, keyToken));
            tokens++;
        }

        // Throws NPE on nulls, retains first inserted value, cannot be modified
        final ImmutableSet<QName> ret = builder.build();
        SourceException.throwIf(ret.size() != tokens, ctx.getStatementSourceReference(),
                "Key argument '%s' contains duplicates", value);
        return ret;
    }

    @Override
    public Set<QName> adaptArgumentValue(final StmtContext<Set<QName>, KeyStatement, KeyEffectiveStatement> ctx,
            final QNameModule targetModule) {
        final Builder<QName> builder = ImmutableSet.builder();
        boolean replaced = false;
        for (final QName qname : ctx.coerceStatementArgument()) {
            if (!targetModule.equals(qname.getModule())) {
                final QName newQname = qname.bindTo(targetModule).intern();
                builder.add(newQname);
                replaced = true;
            } else {
                builder.add(qname);
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
    protected KeyStatement createDeclared(final StmtContext<Set<QName>, KeyStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularKeyStatement(ctx.coerceRawStatementArgument(), ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected KeyStatement createEmptyDeclared(final StmtContext<Set<QName>, KeyStatement, ?> ctx) {
        return new EmptyKeyStatement(ctx.coerceRawStatementArgument(), ctx.coerceStatementArgument());
    }

    @Override
    protected KeyEffectiveStatement createEffective(
            final StmtContext<Set<QName>, KeyStatement, KeyEffectiveStatement> ctx, final KeyStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final Set<QName> arg = ctx.coerceStatementArgument();
        return arg.equals(declared.argument()) ? new RegularLocalKeyEffectiveStatement(declared, substatements)
                : new RegularForeignKeyEffectiveStatement(declared, arg, substatements);
    }

    @Override
    protected KeyEffectiveStatement createEmptyEffective(
            final StmtContext<Set<QName>, KeyStatement, KeyEffectiveStatement> ctx, final KeyStatement declared) {
        final Set<QName> arg = ctx.coerceStatementArgument();
        return arg.equals(declared.argument()) ? new EmptyLocalKeyEffectiveStatement(declared)
                : new EmptyForeignKeyEffectiveStatement(declared, arg);
    }

    static @NonNull Object maskSet(final @NonNull Set<QName> set) {
        return set.size() == 1 ? set.iterator().next() : set;
    }

    @SuppressWarnings("unchecked")
    static @NonNull Set<QName> unmaskSet(final @NonNull Object masked) {
        if (masked instanceof Set) {
            return (Set<QName>) masked;
        }
        verify(masked instanceof QName, "Unexpected argument %s", masked);
        return ImmutableSet.of((QName) masked);
    }
}
