/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.StringRestrictions;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: this class is not used anywhere, decide its future
final class StringRestrictionsEffectiveStatement
        extends DeclaredEffectiveStatementBase<String, StringRestrictions> {
    StringRestrictionsEffectiveStatement(final StmtContext<String, StringRestrictions, ?> ctx) {
        super(ctx);
    }
}
