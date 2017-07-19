/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc8040;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementBase;

/**
 * Effective statement representation of 'yang-data' extension defined in https://tools.ietf.org/html/rfc8040#section-8
 */
@Beta
public final class YangDataEffectiveStatement extends UnknownEffectiveStatementBase<String>
        implements YangDataSchemaNode {

    private final SchemaPath path;
    private final QName maybeQNameArgument;
    private final ContainerSchemaNode containerSchemaNode;

    YangDataEffectiveStatement(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
        super(ctx);

        QName maybeQNameArgumentInit;
        try {
            maybeQNameArgumentInit = StmtContextUtils.qnameFromArgument(ctx, argument());
        } catch (IllegalArgumentException e) {
            maybeQNameArgumentInit = getNodeType();
        }
        this.maybeQNameArgument = maybeQNameArgumentInit;

        path = ctx.getParentContext().getSchemaPath().get().createChild(maybeQNameArgument);
        containerSchemaNode = Preconditions.checkNotNull(firstEffective(ContainerEffectiveStatementImpl.class));
    }

    @Nonnull
    @Override
    public QName getQName() {
        return maybeQNameArgument;
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Nonnull
    @Override
    public ContainerSchemaNode getContainer() {
        return containerSchemaNode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maybeQNameArgument, path);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof YangDataEffectiveStatement)) {
            return false;
        }

        final YangDataEffectiveStatement other = (YangDataEffectiveStatement) obj;
        return Objects.equals(maybeQNameArgument, other.maybeQNameArgument) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("qname", maybeQNameArgument).add("path", path).toString();
    }
}
