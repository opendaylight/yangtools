/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class ThirdPartyExtensionEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<String, ThirdPartyExtensionStatement>
        implements ThirdPartyExtensionEffectiveStatement {

    private final @NonNull SchemaPath path;
    private final String valueFromNamespace;

    ThirdPartyExtensionEffectiveStatementImpl(final Current<String, ThirdPartyExtensionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
        path = stmt.getParent().getSchemaPath().createChild(getNodeType());
        valueFromNamespace = stmt.getFromNamespace(ThirdPartyNamespace.class, stmt.caerbannog());
    }

    @Override
    public String getValueFromNamespace() {
        return valueFromNamespace;
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }
}
