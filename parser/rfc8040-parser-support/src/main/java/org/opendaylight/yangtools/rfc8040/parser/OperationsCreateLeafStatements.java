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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

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

        final var leafSupport = getSupport(YangStmtMapping.LEAF, LeafEffectiveStatement.class);
        final var typeSupport = getSupport(YangStmtMapping.TYPE, TypeEffectiveStatement.class);

        for (var qname : qnames) {
            operations.addEffectiveSubstatement(leafSupport, qname, null)
                .addEffectiveSubstatement(typeSupport, TypeDefinitions.EMPTY, TypeDefinitions.EMPTY.getLocalName());
        }
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        throw new InferenceException(operations, "RPCs failed to transtion to effective model: %s", failed);
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            StatementSupport<X, Y, Z> getSupport(final StatementDefinition def, final Class<Z> effectiveClass) {
        final var tmp = verifyNotNull(operations.getFromNamespace(StatementSupportNamespace.class,
            def.getStatementName()));
        final var repr = tmp.getEffectiveRepresentationClass();
        verify(effectiveClass.equals(repr), "Unexpected support %s representation %s", tmp, repr);

        @SuppressWarnings("unchecked")
        final var ret = (StatementSupport<X, Y, Z>) tmp;
        return ret;
    }
}
