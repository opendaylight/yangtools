/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

final class SPSepToken31 extends AbstractSPSepToken {
    private final int value;

    SPSepToken31(final int line, final int charPositionInLine) {
        value = value31(line, charPositionInLine);
    }

    @Override
    public int getLine() {
        return getLine(value);
    }

    @Override
    public int getCharPositionInLine() {
        return getCharPositionInLine(value);
    }
}
