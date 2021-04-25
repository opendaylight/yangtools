/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString;

public final class EmptyConfigStatement extends ArgumentToString<Boolean> implements ConfigStatement {
    public static final @NonNull EmptyConfigStatement FALSE = new EmptyConfigStatement(false);
    public static final @NonNull EmptyConfigStatement TRUE = new EmptyConfigStatement(true);

    private EmptyConfigStatement(final boolean argument) {
        super(argument);
    }
}
