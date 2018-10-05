/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision;

import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RevisionStatementImpl extends WithArgument<Revision> implements RevisionStatement {
    RevisionStatementImpl(final StmtContext<Revision, RevisionStatement, ?> context) {
        super(context);
    }
}
