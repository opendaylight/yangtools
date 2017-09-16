/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Date;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class RevisionEffectiveStatementImpl extends DeclaredEffectiveStatementBase<Date, RevisionStatement>
        implements DocumentedNode {

    private final String reference;
    private final String description;

    public RevisionEffectiveStatementImpl(final StmtContext<Date, RevisionStatement, ?> ctx) {
        super(ctx);

        final DescriptionEffectiveStatementImpl descStmt = firstEffective(DescriptionEffectiveStatementImpl.class);
        if (descStmt != null) {
            this.description = descStmt.argument();
        } else {
            this.description = null;
        }

        final ReferenceEffectiveStatementImpl refStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        if (refStmt != null) {
            this.reference = refStmt.argument();
        } else {
            this.reference = null;
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getReference() {
        return this.reference;
    }
}
