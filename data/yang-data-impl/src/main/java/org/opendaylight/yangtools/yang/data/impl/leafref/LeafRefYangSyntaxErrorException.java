/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static java.util.Objects.requireNonNull;

public class LeafRefYangSyntaxErrorException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String module;
    private final int line;
    private final int charPositionInLine;

    public LeafRefYangSyntaxErrorException(final String module, final int line, final int charPositionInLine,
            final String message) {
        this(module, line, charPositionInLine, message, null);
    }

    public LeafRefYangSyntaxErrorException(final String module, final int line, final int charPositionInLine,
            final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
        this.module = module;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public String getModule() {
        return module;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public String getFormattedMessage() {
        final StringBuilder sb = new StringBuilder(getMessage());
        if (module != null) {
            sb.append(" in module ");
            sb.append(module);
        }
        if (line != 0) {
            sb.append(" on line ");
            sb.append(line);
            if (charPositionInLine != 0) {
                sb.append(" character ");
                sb.append(charPositionInLine);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + getFormattedMessage();
    }
}
