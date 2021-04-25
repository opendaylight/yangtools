/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.Status;

public final class EmptyStatusStatement extends AbstractStatusStatement {
    public static final @NonNull EmptyStatusStatement CURRENT = new EmptyStatusStatement(Status.CURRENT);
    public static final @NonNull EmptyStatusStatement DEPRECATED = new EmptyStatusStatement(Status.DEPRECATED);
    public static final @NonNull EmptyStatusStatement OBSOLETE = new EmptyStatusStatement(Status.OBSOLETE);

    private EmptyStatusStatement(final Status argument) {
        super(argument);
    }
}
