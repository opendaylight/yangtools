/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyRequireInstanceEffectiveStatement
        extends DefaultArgument<Boolean, @NonNull RequireInstanceStatement>
        implements RequireInstanceEffectiveStatement {
    public static final @NonNull EmptyRequireInstanceEffectiveStatement FALSE =
        new EmptyRequireInstanceEffectiveStatement(EmptyRequireInstanceStatement.FALSE);
    public static final @NonNull EmptyRequireInstanceEffectiveStatement TRUE =
        new EmptyRequireInstanceEffectiveStatement(EmptyRequireInstanceStatement.TRUE);

    public EmptyRequireInstanceEffectiveStatement(final @NonNull RequireInstanceStatement declared) {
        super(declared);
    }
}
