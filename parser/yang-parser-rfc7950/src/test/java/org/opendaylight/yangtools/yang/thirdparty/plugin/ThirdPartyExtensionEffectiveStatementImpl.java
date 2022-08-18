/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class ThirdPartyExtensionEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<String, ThirdPartyExtensionStatement>
        implements ThirdPartyExtensionEffectiveStatement {
    private final String valueFromNamespace;

    ThirdPartyExtensionEffectiveStatementImpl(final Current<String, ThirdPartyExtensionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.declared(), stmt.argument(), stmt.history(), substatements);
        valueFromNamespace = stmt.getFromNamespace(ThirdPartyNamespace.INSTANCE, Empty.value());
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
    public ThirdPartyExtensionEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
