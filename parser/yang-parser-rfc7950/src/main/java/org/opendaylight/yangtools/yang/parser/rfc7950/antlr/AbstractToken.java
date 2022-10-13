/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static com.google.common.base.Preconditions.checkState;

import org.antlr.v4.runtime.WritableToken;

abstract sealed class AbstractToken implements WritableToken permits AbstractSourceToken, ExplicitTextToken {
    private int tokenIndex = -1;

    @Override
    public final int getChannel() {
        return DEFAULT_CHANNEL;
    }

    @Override
    public final int getTokenIndex() {
        return tokenIndex;
    }

    @Override
    public final void setTokenIndex(final int index) {
        checkState(tokenIndex == -1);
        tokenIndex = index;
    }

    @Override
    public final void setText(final String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setType(final int ttype) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setLine(final int line) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setCharPositionInLine(final int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setChannel(final int channel) {
        throw new UnsupportedOperationException();
    }
}
