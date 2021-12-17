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
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument;

public final class EmptyConfigStatement extends WithArgument implements ConfigStatement {
    private static final @NonNull EmptyConfigStatement FALSE = new EmptyConfigStatement("false");
    private static final @NonNull EmptyConfigStatement TRUE = new EmptyConfigStatement("true");

    private EmptyConfigStatement(final String rawArgument) {
        super(rawArgument);
    }

    public static @NonNull EmptyConfigStatement of(final String arg) {
        switch (arg) {
            case "false":
                return FALSE;
            case "true":
                return TRUE;
            default:
                throw new IllegalArgumentException("Unrecognized config argument \"" + arg + "\"");
        }
    }
}
