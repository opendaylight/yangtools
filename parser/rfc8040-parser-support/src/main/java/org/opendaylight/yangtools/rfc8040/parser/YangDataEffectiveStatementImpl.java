/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

@Beta
final class YangDataEffectiveStatementImpl extends AbstractEffectiveUnknownSchmemaNode<String, YangDataStatement>
        implements YangDataEffectiveStatement, YangDataSchemaNode {
    private final @NonNull QName argumentQName;
    private final @NonNull ContainerEffectiveStatement container;

    YangDataEffectiveStatementImpl(final Current<String, YangDataStatement> stmt,
             final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName qname) {
        super(stmt.declared(), stmt.argument(), stmt.history(), substatements);
        argumentQName = requireNonNull(qname);

        container = findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).get();

        // TODO: this is strong binding of two API contracts. Unfortunately ContainerEffectiveStatement design is
        //       incomplete.
        verify(container instanceof ContainerSchemaNode, "Incompatible container %s", container);
    }

    @Override
    public QName getQName() {
        return argumentQName;
    }

    @Override
    public ContainerEffectiveStatement getContainer() {
        return container;
    }

    @Override
    public ContainerSchemaNode getContainerSchemaNode() {
        // Verified in the constructor
        return (ContainerSchemaNode) container;
    }

    @Override
    public YangDataEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final Class<N> namespace) {
        if (SchemaTreeAwareEffectiveStatement.Namespace.class.equals(namespace)
            || DataTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
            @SuppressWarnings("unchecked")
            final Map<K, V> ns = (Map<K, V>)Map.of(container.argument(), container);
            return Optional.of(ns);
        }
        return super.getNamespaceContents(namespace);
    }
}
