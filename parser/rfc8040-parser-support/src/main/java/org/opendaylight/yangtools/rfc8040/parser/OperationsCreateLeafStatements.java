/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Once we have identified the {@code operations} container we want to enrich, we need to identify all RPC statements
 * in the reactor. For that we need that all sources complete {@link ModelProcessingPhase#FULL_DECLARATION} -- after
 * which we are still not done, as not all those RPCs may make it to the resulting {@link EffectiveModelContext}.
 */
final class OperationsCreateLeafStatements implements InferenceAction {
    private final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs;
    private final Mutable<?, ?, ?> operations;

    private OperationsCreateLeafStatements(final Mutable<?, ?, ?> operations,
            final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs) {
        this.operations = requireNonNull(operations);
        this.prereqs = requireNonNull(prereqs);
    }

    static void applyTo(final StmtContext<?, ?, ?> ietfRestconfModule, final Mutable<?, ?, ?> operations) {
        final var action = operations.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        action.mutatesEffectiveCtx(operations);

        final var prereqs = new ArrayList<Prerequisite<? extends StmtContext<?, ?, ?>>>();
        // FIXME: this not accurate: we need all sources, not just modules
        for (var module : ietfRestconfModule.getAllFromNamespace(ParserNamespaces.MODULE).values()) {
            if (!ietfRestconfModule.equals(module)) {
                prereqs.add(action.requiresCtx((StmtContext<?, ?, ?>)module, ModelProcessingPhase.EFFECTIVE_MODEL));
            }
        }

        action.apply(new OperationsCreateLeafStatements(operations, prereqs));
    }

    @Override
    public void apply(final InferenceContext ctx) {
        // Enumerate all RPCs that can be built
        final var qnames = prereqs.stream()
            .flatMap(prereq -> prereq.resolve(ctx).declaredSubstatements().stream())
            .filter(stmt -> stmt.producesDeclared(RpcStatement.class)
                && stmt.isSupportedToBuildEffective() && stmt.isSupportedByFeatures())
            .map(stmt -> (QName) stmt.argument())
            // predictable order...
            .sorted(Comparator.naturalOrder())
            // each QName should be distinct, but let's make sure anyway
            .distinct()
            .collect(Collectors.toUnmodifiableList());

        if (!qnames.isEmpty()) {
            final var leafSupport = getSupport(YangStmtMapping.LEAF, LeafEffectiveStatement.class);
            final var typeSupport = getSupport(YangStmtMapping.TYPE, TypeEffectiveStatement.class);

            for (var qname : qnames) {
                final var leaf = operations.createUndeclaredSubstatement(leafSupport, qname);
                leaf.addEffectiveSubstatement(leaf.createUndeclaredSubstatement(typeSupport, TypeDefinitions.EMPTY));
                operations.addEffectiveSubstatement(leaf);
            }
        }
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        // We do not really need to fail, as this means reactor will fail anyway
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            StatementSupport<X, Y, Z> getSupport(final StatementDefinition def, final Class<Z> effectiveClass) {
        final var tmp = verifyNotNull(operations.getFromNamespace(StatementSupport.NAMESPACE,
            def.getStatementName()));
        final var repr = tmp.definition().getEffectiveRepresentationClass();
        verify(effectiveClass.equals(repr), "Unexpected support %s representation %s", tmp, repr);

        @SuppressWarnings("unchecked")
        final var ret = (StatementSupport<X, Y, Z>) tmp;
        return ret;
    }
}
