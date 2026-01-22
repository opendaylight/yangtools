/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyYangVersionStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyYangVersionEffectiveStatement
        extends DefaultArgument<YangVersion, @NonNull YangVersionStatement> implements YangVersionEffectiveStatement {
    public static final @NonNull EmptyYangVersionEffectiveStatement VERSION_1 =
        new EmptyYangVersionEffectiveStatement(EmptyYangVersionStatement.VERSION_1);
    public static final @NonNull EmptyYangVersionEffectiveStatement VERSION_1_1 =
        new EmptyYangVersionEffectiveStatement(EmptyYangVersionStatement.VERSION_1_1);

    public EmptyYangVersionEffectiveStatement(final @NonNull YangVersionStatement declared) {
        super(declared);
    }
}
