/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static java.util.Objects.requireNonNull;

final class ExplicitTextToken extends AbstractTextToken {
    private final int type;
    private final String text;

    ExplicitTextToken(final int type, final String text) {
        this.type = type;
        this.text = requireNonNull(text);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getLine() {
        // TODO: this mimics CommonToken, but is probably not right
        return 0;
    }

    @Override
    public int getCharPositionInLine() {
        return -1;
    }

    @Override
    public int getStartIndex() {
        // TODO: this mimics CommonToken, but is probably not right
        return 0;
    }

    @Override
    public int getStopIndex() {
        // TODO: this mimics CommonToken, but is probably not right
        return 0;
    }
}
