/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString;

public final class EmptyRequireInstanceStatement extends ArgumentToString<Boolean> implements RequireInstanceStatement {
    public static final @NonNull EmptyRequireInstanceStatement FALSE = new EmptyRequireInstanceStatement(false);
    public static final @NonNull EmptyRequireInstanceStatement TRUE = new EmptyRequireInstanceStatement(true);

    private EmptyRequireInstanceStatement(final boolean argument) {
        super(argument);
    }
}
