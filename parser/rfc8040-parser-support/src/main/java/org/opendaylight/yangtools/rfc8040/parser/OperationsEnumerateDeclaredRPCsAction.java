/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Once we have identified the {@code operations} container we want to enrich, we need to identify all RPC statements
 * in the reactor. For that we need that all sources complete {@link ModelProcessingPhase#FULL_DECLARATION} -- after
 * which we are still not done, as not all those RPCs may make it to the resulting {@link EffectiveModelContext}.
 */
final class OperationsEnumerateDeclaredRPCsAction implements InferenceAction {
    private final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs;
    private final Mutable<?, ?, ?> operations;

    private OperationsEnumerateDeclaredRPCsAction(final Mutable<?, ?, ?> operations,
            final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs) {
        this.operations = requireNonNull(operations);
        this.prereqs = requireNonNull(prereqs);
    }

    static void applyTo(final Mutable<?, ?, ?> ietfRestconfModule, final Mutable<?, ?, ?> operations) {
        final var action = operations.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);

        final var prereqs = new ArrayList<Prerequisite<? extends StmtContext<?, ?, ?>>>();
        // FIXME: this not accurate: we need all sources, not just modules
        for (var module : ietfRestconfModule.getAllFromNamespace(ModuleNamespace.class).values()) {
            if (!ietfRestconfModule.equals(module)) {
                prereqs.add(action.requiresCtx((StmtContext<?, ?, ?>)module, ModelProcessingPhase.FULL_DECLARATION));
            }
        }

        action.apply(new OperationsEnumerateDeclaredRPCsAction(operations, prereqs));
    }

    @Override
    public void apply(final InferenceContext ctx) {
        // Enumerate all RPCs and require them all to transition have effective model before we proceed.
        final var action = operations.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final var rpcPrereqs = prereqs.stream()
            .flatMap(prereq -> prereq.resolve(ctx).declaredSubstatements().stream())
            .filter(stmt -> stmt.producesDeclared(RpcStatement.class)
                && stmt.isSupportedToBuildEffective() && stmt.isSupportedByFeatures())
            .map(rpc -> action.requiresCtx((StmtContext<?, ?, ?>)rpc, ModelProcessingPhase.EFFECTIVE_MODEL))
            .collect(Collectors.toUnmodifiableList());

        action.apply(new OperationsCreateLeafStatements(operations, List.copyOf(rpcPrereqs)));
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        // We do not really need to fail, as this means reactor will fail anyway
    }
}
