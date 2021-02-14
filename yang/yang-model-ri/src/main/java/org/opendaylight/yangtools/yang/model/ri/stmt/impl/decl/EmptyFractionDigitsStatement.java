/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString;

public final class EmptyFractionDigitsStatement extends ArgumentToString<Integer> implements FractionDigitsStatement {
    private static final @NonNull EmptyFractionDigitsStatement[] INSTANCES;

    static {
        final EmptyFractionDigitsStatement[] inst = new EmptyFractionDigitsStatement[18];
        for (int i = 0; i < 18; ++i) {
            inst[i] =  new EmptyFractionDigitsStatement(i + 1);
        }
        INSTANCES = inst;
    }

    private EmptyFractionDigitsStatement(final int argument) {
        super(argument);
    }

    public static @NonNull EmptyFractionDigitsStatement of(final int argument) {
        try {
            return INSTANCES[argument - 1];
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid fraction-digits argument " + argument, e);
        }
    }
}
