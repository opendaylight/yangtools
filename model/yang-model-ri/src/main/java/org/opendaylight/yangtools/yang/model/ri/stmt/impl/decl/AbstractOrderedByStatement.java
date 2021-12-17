/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement;

abstract class AbstractOrderedByStatement extends AbstractDeclaredStatement
        implements OrderedByStatement {
    private final @NonNull Ordering argument;

    AbstractOrderedByStatement(final Ordering argument) {
        this.argument = requireNonNull(argument);
    }

    @Override
    public final Ordering argument() {
        return argument;
    }

    @Override
    public final @NonNull String rawArgument() {
        return argument.argument();
    }
}
