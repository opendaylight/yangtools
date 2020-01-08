/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class BaseStatementSupport<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends AbstractStatementSupport<A, D, E> {
    protected BaseStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    public final E createEffective(final StmtContext<A, D, E> ctx) {
        final D declared = buildDeclared(ctx);
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements = buildEffectiveSubstatements(ctx);
        return substatements.isEmpty() ? createEmptyEffective(ctx, declared)
                : createEffective(ctx, declared, substatements);
    }

    protected abstract @NonNull E createEffective(@NonNull StmtContext<A, D, E> ctx, @NonNull D declared,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    protected abstract @NonNull E createEmptyEffective(@NonNull StmtContext<A, D, E> ctx, @NonNull D declared);

    static final <A, D extends DeclaredStatement<A>> @NonNull D buildDeclared(final StmtContext<A, D, ?> ctx) {
        /*
         * Share original instance of declared statement between all effective
         * statements which have been copied or derived from this original
         * declared statement.
         */
        @SuppressWarnings("unchecked")
        final StmtContext<?, D, ?> lookupCtx = (StmtContext<?, D, ?>) ctx.getOriginalCtx().orElse(ctx);
        return verifyNotNull(lookupCtx.buildDeclared(), "Statement %s failed to build declared statement", lookupCtx);
    }

    /**
     * Create a set of substatements. This method is split out so it can be overridden in
     * ExtensionEffectiveStatementImpl to leak a not-fully-initialized instance.
     *
     * @param substatements proposed substatements
     * @return Filtered substatements
     */
    private static ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        return substatements.stream()
                .filter(StmtContext::isSupportedToBuildEffective)
                .map(StmtContext::buildEffective)
                .collect(ImmutableList.toImmutableList());
    }

    static final ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final StmtContext<?, ?, ?> ctx) {
        return buildEffectiveSubstatements(declaredSubstatements(ctx));
    }

    static final @NonNull List<StmtContext<?, ?, ?>> declaredSubstatements(final StmtContext<?, ?, ?> ctx) {
        /*
         * This dance is required to ensure that effects of 'uses' nodes are applied in the same order as
         * the statements were defined -- i.e. if we have something like this:
         *
         * container foo {
         *   uses bar;
         *   uses baz;
         * }
         *
         * grouping baz {
         *   leaf baz {
         *     type string;
         *   }
         * }
         *
         * grouping bar {
         *   leaf bar {
         *     type string;
         *   }
         * }
         *
         * The reactor would first inline 'uses baz' as that definition is the first one completely resolved and then
         * inline 'uses bar'. Here we are iterating in declaration order re-inline the statements.
         *
         * FIXME: 5.0.0: this really should be handled by UsesStatementSupport such that 'uses baz' would have a
         *               prerequisite of a resolved 'uses bar'.
         */
        final List<StmtContext<?, ?, ?>> substatementsInit = new ArrayList<>();
        Set<StmtContext<?, ?, ?>> filteredStatements = null;
        for (final StmtContext<?, ?, ?> declaredSubstatement : ctx.declaredSubstatements()) {
            if (declaredSubstatement.isSupportedByFeatures()) {
                substatementsInit.add(declaredSubstatement);

                final Collection<? extends StmtContext<?, ?, ?>> effect = declaredSubstatement.getEffectOfStatement();
                if (!effect.isEmpty()) {
                    if (filteredStatements == null) {
                        filteredStatements = new HashSet<>();
                    }
                    filteredStatements.addAll(effect);
                    substatementsInit.addAll(effect);
                }
            }
        }

        if (filteredStatements != null) {
            for (StmtContext<?, ?, ?> stmt : ctx.effectiveSubstatements()) {
                if (!filteredStatements.contains(stmt)) {
                    substatementsInit.add(stmt);
                }
            }
        } else {
            substatementsInit.addAll(ctx.effectiveSubstatements());
        }

        return substatementsInit;
    }
}
