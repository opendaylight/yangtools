/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

final class IRStatement31 extends IRStatement {
    private final int value;

    IRStatement31(final IRKeyword keyword, final int startLine, final int startColumn) {
        super(keyword);
        this.value = value31(startLine, startColumn);
    }

    @Override
    public int startLine() {
        return startLine(value);
    }

    @Override
    public int startColumn() {
        return startColumn(value);
    }
}
