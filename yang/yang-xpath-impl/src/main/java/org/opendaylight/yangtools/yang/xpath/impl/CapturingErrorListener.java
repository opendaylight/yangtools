/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import javax.xml.xpath.XPathExpressionException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.Nullable;

final class CapturingErrorListener extends BaseErrorListener {
    private @Nullable XPathExpressionException error;

    @Override
    public void syntaxError(final @Nullable Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol,
            final int line, final int charPositionInLine, final @Nullable String msg,
            final @Nullable RecognitionException cause) {
        final XPathExpressionException ex = Utils.wrapException(cause, "%s", msg);
        if (error == null) {
            error = ex;
        } else {
            error.addSuppressed(ex);
        }
    }

    void reportError() throws XPathExpressionException {
        if (error != null) {
            throw error;
        }
    }
}
