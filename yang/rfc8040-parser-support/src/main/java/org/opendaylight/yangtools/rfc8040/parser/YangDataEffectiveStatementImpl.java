/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

@Beta
final class YangDataEffectiveStatementImpl extends UnknownEffectiveStatementBase<String, YangDataStatement>
        implements YangDataEffectiveStatement, YangDataSchemaNode {

    private final @NonNull SchemaPath path;
    private final @NonNull QName maybeQNameArgument;
    private final @NonNull ContainerEffectiveStatement container;

    YangDataEffectiveStatementImpl(final Current<String, YangDataStatement> stmt,
             final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);

        QName maybeQNameArgumentInit;
        try {
            maybeQNameArgumentInit = StmtContextUtils.parseIdentifier(stmt.caerbannog(), argument());
        } catch (IllegalArgumentException e) {
            maybeQNameArgumentInit = getNodeType();
        }
        this.maybeQNameArgument = maybeQNameArgumentInit;

        path = stmt.getParent().getSchemaPath().createChild(maybeQNameArgument);
        container = findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).get();

        // TODO: this is strong binding of two API contracts. Unfortunately ContainerEffectiveStatement design is
        //       incomplete.
        Verify.verify(container instanceof ContainerSchemaNode);
    }

    @Override
    public @NonNull QName getQName() {
        return maybeQNameArgument;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
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
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("qname", maybeQNameArgument)
                .add("path", path)
                .add("container", container).toString();
    }
}
