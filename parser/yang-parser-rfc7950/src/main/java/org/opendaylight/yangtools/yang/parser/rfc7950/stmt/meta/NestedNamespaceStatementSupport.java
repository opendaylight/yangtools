/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.UserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Common base class for {@link GroupingStatementSupport} and {@link TypedefStatementSupport} providing helpers around
 * declaration shadoing as defined in
 * <a href="https://www.rfc-editor.org/info/rfc7950/#section-5.5">RFC7590 section 5.5</a>.
 */
abstract sealed class NestedNamespaceStatementSupport<
        D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends AbstractQNameStatementSupport<D, E>
        permits GroupingStatementSupport {
    private final @NonNull UserNamespace<QName, StmtContext<QName, D, E>> namespace;

    NestedNamespaceStatementSupport(final StatementDefinition<QName, D, E> publicDefinition,
            final StatementPolicy<QName, D> policy, final SubtreePolicy subtreePolicy,
            final YangParserConfiguration config, final SubstatementValidator validator,
            final UserNamespace<QName, StmtContext<QName, D, E>> namespace) {
        super(publicDefinition, policy, subtreePolicy, config, validator);
        this.namespace = requireNonNull(namespace);
    }

    @Override
    public final void onStatementDefinitionDeclared(final Mutable<QName, D, E> stmt) {
        // As per RFC7950, section 5.5:
        //    Scoped definitions MUST NOT shadow definitions at a higher scope.  A
        //    type or grouping cannot be defined if a higher level in the statement
        //    hierarchy has a definition with a matching identifier
        final var parent = stmt.coerceParentContext();

        // Considering this model:
        //
        //   module foo {
        //     container bar {
        //       grouping bar { ... }
        //
        //       container baz {
        //         grouping bar { ... }
        //         typedef baz { ... }
        //       }
        //     }
        //
        //     grouping foo { ... }
        //     typedef baz { ... }
        //  }
        //
        // There are two distinct patterns in which an overlap occurs:
        // - foo has a higher scope definition BEFORE the lower scope definition is encountered
        // - baz has a higher scope definition AFTER the lower scope definition is encountered

        // namespace behaviour and storage operations deal with the first case
        parent.putToNs(namespace, stmt.getArgument(), stmt, this::newSourceException);

        final var higherScope = parent.getParentContext();
        if (higherScope == null) {
            // i.e. parent is a top level statement, so stmt is a top-level grouping exported from the defining module
            return;
        }

        // Once this statement's root completes STATEMENT_DEFININITION, higher scope should not have a competing
        // definition. Root completion implies all substatements have been processed and therefore all higher scopes
        // have been populated.
        final var action = stmt.newInferenceAction(ModelProcessingPhase.STATEMENT_DEFINITION);
        action.requiresCtx(higherScope.getRoot(), ModelProcessingPhase.STATEMENT_DEFINITION);
        action.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final var prev = higherScope.namespaceItem(ParserNamespaces.GROUPING, stmt.getArgument());
                if (prev != null) {
                    throw newSourceException(prev, stmt);
                }
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                // No-op
            }
        });
    }

    @NonNullByDefault
    abstract SourceException newSourceException(StmtContext<?, ?, ?> prev, StmtContext<?, ?, ?> next);
}
