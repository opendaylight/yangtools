/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.ir.IRStatement;

final class IRStatementIterator extends AbstractIterator<@NonNull String> {
    private final IRStatement statement;

    IRStatementIterator(final IRStatement statement) {
        this.statement = requireNonNull(statement);

    }
    @Override
    protected String computeNext() {
        // TODO Auto-generated method stub
        return null;
    }
}
