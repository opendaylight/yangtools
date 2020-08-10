/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Private implementation of {@link ThirdPartyExtensionStatement}. This class can rely on parser internals and should
 * never be exposed to the world.
 */
final class ThirdPartyExtensionStatementImpl extends WithSubstatements implements ThirdPartyExtensionStatement {
    ThirdPartyExtensionStatementImpl(final StmtContext<String, ThirdPartyExtensionStatement, ?> context,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(context, substatements);
    }
}
