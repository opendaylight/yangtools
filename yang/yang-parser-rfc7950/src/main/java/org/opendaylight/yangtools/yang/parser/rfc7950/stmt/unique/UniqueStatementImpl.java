/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class UniqueStatementImpl extends AbstractDeclaredStatement<Collection<SchemaNodeIdentifier.Relative>>
        implements UniqueStatement {
    UniqueStatementImpl(final StmtContext<Collection<SchemaNodeIdentifier.Relative>, UniqueStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public Collection<Relative> getTag() {
        return argument();
    }
}
