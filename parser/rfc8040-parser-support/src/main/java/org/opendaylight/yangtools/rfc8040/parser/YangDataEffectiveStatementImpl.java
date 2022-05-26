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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

@Beta
final class YangDataEffectiveStatementImpl extends AbstractEffectiveUnknownSchmemaNode<String, YangDataStatement>
        implements YangDataEffectiveStatement, YangDataSchemaNode, DataNodeContainerMixin<String, YangDataStatement> {
    private final @NonNull DataSchemaNode child;

    YangDataEffectiveStatementImpl(final Current<String, YangDataStatement> stmt,
             final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final DataSchemaNode child) {
        super(stmt.declared(), stmt.argument(), stmt.history(), substatements);
        this.child = requireNonNull(child);
    }

    @Override
    public QName getQName() {
        return child.getQName();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return name.equals(child.getQName()) ? child : null;
    }

    @Override
    public YangDataEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final Class<N> namespace) {
        if (SchemaTreeNamespace.class.equals(namespace)) {
            return castChild();
        }
        if (DataTreeNamespace.class.equals(namespace)) {
            if (child instanceof DataTreeEffectiveStatement) {
                return castChild();
            }

            // A schema tree statement which *has to* know about data tree -- just forward it
            verify(child instanceof DataTreeAwareEffectiveStatement, "Unexpected child %s", child);
            return Optional.of(((DataTreeAwareEffectiveStatement<?, ?>) child).getAll(namespace));
        }
        return super.getNamespaceContents(namespace);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Optional<Map<K, V>> castChild() {
        return Optional.of((Map<K, V>) Map.of(child.getQName(), child));
    }
}
