/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString;

public final class EmptyYinElementStatement extends ArgumentToString<Boolean> implements YinElementStatement {
    public static final @NonNull EmptyYinElementStatement FALSE = new EmptyYinElementStatement(false);
    public static final @NonNull EmptyYinElementStatement TRUE = new EmptyYinElementStatement(true);

    private EmptyYinElementStatement(final boolean argument) {
        super(argument);
    }
}
