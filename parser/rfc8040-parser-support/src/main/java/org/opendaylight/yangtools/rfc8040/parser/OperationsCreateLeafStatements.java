/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

/**
 * @author nite
 *
 */
final class OperationsCreateLeafStatements implements InferenceAction {
    private final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs;
    private final @NonNull Mutable<?, ?, ?> operations;

    OperationsCreateLeafStatements(final Mutable<?, ?, ?> operations,
            final List<Prerequisite<? extends StmtContext<?, ?, ?>>> prereqs) {
        this.operations = requireNonNull(operations);
        this.prereqs = requireNonNull(prereqs);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final var qnames = prereqs.stream()
            .map(prereq -> prereq.resolve(ctx))
            .filter(stmt -> stmt.isSupportedToBuildEffective() && stmt.isSupportedByFeatures())
            .map(stmt -> (QName) stmt.argument())
            // predictable order...
            .sorted(Comparator.naturalOrder())
            // each QName should be distinct, but let's make sure anyway
            .distinct()
            .collect(Collectors.toUnmodifiableList());

        final var leafSupport = operations.getFromNamespace(StatementSupportNamespace.class,
            YangStmtMapping.LEAF.getStatementName());
        for (var qname : qnames) {
            ((StatementContextBase<?, ?, ?>) operations).appendImplicitSubstatement(leafSupport, null);
        }
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        throw new InferenceException(operations, "RPCs failed to transtion to effective model: %s", failed);
    }

}
