/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import java.text.ParseException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

final class CapturingErrorListener extends BaseErrorListener {
    private ParseException error;

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException cause) {
        final ParseException ex = Utils.wrapException(cause, "%s", msg);
        if (error == null) {
            error = ex;
        } else {
            error.addSuppressed(ex);
        }
    }

    void reportError() throws ParseException {
        if (error != null) {
            throw error;
        }
    }
}
