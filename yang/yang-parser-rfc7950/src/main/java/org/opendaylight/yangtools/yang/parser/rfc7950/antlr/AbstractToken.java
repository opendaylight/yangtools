/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import org.antlr.v4.runtime.Token;

abstract class AbstractToken implements Token {
    @Override
    public final int getChannel() {
        return DEFAULT_CHANNEL;
    }

    @Override
    public final int getTokenIndex() {
        // This is a loss of functionality when compared to CommonToken, but we do not seem to be using this facility
        // anyway and it does make things quite a bit more efficient.
        throw new UnsupportedOperationException();
    }
}
