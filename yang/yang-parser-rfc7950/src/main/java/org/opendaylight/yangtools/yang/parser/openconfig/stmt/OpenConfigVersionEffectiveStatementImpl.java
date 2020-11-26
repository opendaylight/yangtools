/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.SchemaNodePropertyUtil;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class OpenConfigVersionEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<SemVer, OpenConfigVersionStatement>
        implements OpenConfigVersionEffectiveStatement {

    private final @NonNull SchemaPath path;

    OpenConfigVersionEffectiveStatementImpl(final Current<SemVer, OpenConfigVersionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
        path = stmt.getParent().getSchemaPath().createChild(getNodeType());
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        if (SchemaNodePropertyUtil.isGetPathForbidden()) {
            throw new UnsupportedOperationException(SchemaNodePropertyUtil.SCHEMANODE_GETPATH_FORBID_PROPERTY
                    + " property enabled - creating the SchemaPath is forbidden in SchemaNode");
        }
        return path;
    }
}
