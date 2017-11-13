/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yang_version;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class YangVersionStatementImpl extends AbstractDeclaredStatement<YangVersion> implements YangVersionStatement {
    YangVersionStatementImpl(final StmtContext<YangVersion, YangVersionStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public YangVersion getValue() {
        return argument();
    }
}
