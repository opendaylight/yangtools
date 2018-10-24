/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config;

import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class ConfigEffectiveStatementImpl extends WithArgument<Boolean, ConfigStatement>
        implements ConfigEffectiveStatement {
    public ConfigEffectiveStatementImpl(final StmtContext<Boolean, ConfigStatement, ?> ctx) {
        super(ctx);
    }
}
