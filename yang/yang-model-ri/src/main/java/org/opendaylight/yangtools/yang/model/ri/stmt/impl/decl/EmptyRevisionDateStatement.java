/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString;

public final class EmptyRevisionDateStatement extends ArgumentToString<Revision> implements RevisionDateStatement {
    public EmptyRevisionDateStatement(final Revision argument) {
        super(argument);
    }
}
