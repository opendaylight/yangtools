/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.parser.rfc6020.util.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class UniqueEffectiveStatementImpl
        extends DeclaredEffectiveStatementBase<Collection<Relative>, UniqueStatement>
        implements UniqueConstraint, UniqueEffectiveStatement {
    public UniqueEffectiveStatementImpl(final StmtContext<Collection<Relative>, UniqueStatement, ?> ctx) {
        super(ctx);
    }

    @Nonnull
    @Override
    public Collection<Relative> getTag() {
        return argument();
    }
}
