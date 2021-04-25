/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString;

public final class EmptyYangVersionStatement extends ArgumentToString<YangVersion> implements YangVersionStatement {
    public static final @NonNull EmptyYangVersionStatement VERSION_1 =
        new EmptyYangVersionStatement(YangVersion.VERSION_1);
    public static final @NonNull EmptyYangVersionStatement VERSION_1_1 =
        new EmptyYangVersionStatement(YangVersion.VERSION_1_1);

    private EmptyYangVersionStatement(final @NonNull YangVersion argument) {
        super(argument);
    }
}
