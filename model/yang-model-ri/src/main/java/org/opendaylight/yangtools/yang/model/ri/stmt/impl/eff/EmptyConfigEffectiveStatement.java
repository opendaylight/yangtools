/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyConfigStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyConfigEffectiveStatement extends DefaultArgument<Boolean, @NonNull ConfigStatement>
        implements ConfigEffectiveStatement {
    public static final @NonNull EmptyConfigEffectiveStatement FALSE =
        new EmptyConfigEffectiveStatement(EmptyConfigStatement.FALSE);
    public static final @NonNull EmptyConfigEffectiveStatement TRUE =
        new EmptyConfigEffectiveStatement(EmptyConfigStatement.TRUE);

    public EmptyConfigEffectiveStatement(final @NonNull ConfigStatement declared) {
        super(declared);
    }
}
