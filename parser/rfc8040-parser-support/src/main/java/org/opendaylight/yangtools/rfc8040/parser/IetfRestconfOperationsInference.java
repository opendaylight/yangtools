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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * An
 *
 * @author nite
 *
 */
final class IetfRestconfOperationsInference implements InferenceAction {
    private final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs;

    private IetfRestconfOperationsInference(final Mutable<?, ?, ?> operations,
            final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs) {
        this.prereqs = requireNonNull(prereqs);
    }

    static void applyTo(final Mutable<?, ?, ?> ietfRestconfModule, final Mutable<?, ?, ?> operations) {
        final var action = operations.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);

        final var prereqs = new ArrayList<Prerequisite<? extends StmtContext<?, ?, ?>>>();
        for (var module : ietfRestconfModule.getAllFromNamespace(ModuleNamespace.class).values()) {
            if (!ietfRestconfModule.equals(module)) {
                prereqs.add(action.requiresCtx((StmtContext<?, ?, ?>)module, ModelProcessingPhase.FULL_DECLARATION));
            }
        }

        action.apply(new IetfRestconfOperationsInference(operations, prereqs));
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final Set<QName> rpcNames = new HashSet<>();
        for (var prereq : prereqs) {
            for (var stmt : prereq.resolve(ctx).declaredSubstatements()) {
                // FIXME: this is insufficient :(
                //
                // The problem is that 'deviate' effects are likely unaccounted for, which is tricky, because we should
                // not hold up operations container becoming effective because it might be used by a 'uses' in one of
                // the modules -- and hence we cannot wait for them modules to reach effective model.
                //
                // What we need to do here instead is hook up yet another inference action, waiting for the RPC node
                // to become effective... right?
                if (stmt.producesDeclared(RpcStatement.class) && stmt.isSupportedToBuildEffective()
                    && stmt.isSupportedByFeatures()) {
                    rpcNames.add((QName) stmt.argument());
                }
            }
        }

        // Predictable order
        final var qnames = new ArrayList<>(rpcNames);
        qnames.sort(Comparator.naturalOrder());
        for (var qname : qnames) {
            // FIXME: add an Undeclared leaf statement
        }
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        // We do not really need to fail, as this means reactor will fail anyway
    }
}
