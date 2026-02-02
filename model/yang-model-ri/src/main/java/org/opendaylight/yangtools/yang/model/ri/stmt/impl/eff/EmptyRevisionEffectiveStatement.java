/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyRevisionEffectiveStatement extends DefaultArgument<Revision, @NonNull RevisionStatement>
        implements RevisionEffectiveStatement, DocumentedNode.Mixin<RevisionEffectiveStatement> {
    public EmptyRevisionEffectiveStatement(final @NonNull RevisionStatement declared) {
        super(declared);
    }

    @Override
    public RevisionEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
