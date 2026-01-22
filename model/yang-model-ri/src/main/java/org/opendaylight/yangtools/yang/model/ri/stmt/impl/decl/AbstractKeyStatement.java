/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithRawArgument;

abstract class AbstractKeyStatement extends WithRawArgument<KeyArgument> implements KeyStatement {
    private final @NonNull Object argument;

    AbstractKeyStatement(final String rawArgument, final @NonNull KeyArgument argument) {
        super(rawArgument);
        this.argument = EmptyKeyStatement.maskArgument(argument);
    }

    @Override
    public final KeyArgument argument() {
        return EmptyKeyStatement.unmaskArgument(argument);
    }
}
