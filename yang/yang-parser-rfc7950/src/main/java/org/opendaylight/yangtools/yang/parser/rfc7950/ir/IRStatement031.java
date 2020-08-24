/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

final class IRStatement031 extends IRStatement {
    private final int value;

    IRStatement031(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn) {
        super(keyword, argument);
        this.value = startLine << 8 | startColumn & 0xFF;
    }

    @Override
    public int startLine() {
        return value >>> 8;
    }

    @Override
    public int startColumn() {
        return value & 0xFF;
    }

    @Override
    byte ioType() {
        return IOSupport.STMT_031;
    }

    int value() {
        return value;
    }
}
