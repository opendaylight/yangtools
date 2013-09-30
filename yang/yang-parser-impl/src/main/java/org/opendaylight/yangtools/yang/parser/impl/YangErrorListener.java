/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

final class YangErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        String module = getModuleName(recognizer);
        throw new YangParseException(module, line, msg);
    }

    private String getModuleName(Recognizer<?, ?> recognizer) {
        String result;
        if (recognizer instanceof Parser) {
            try {
                Parser parser = (Parser) recognizer;
                String model = parser.getInputStream().getTokenSource().getInputStream().toString();
                model = model.substring(0, model.indexOf("\n"));
                model = model.substring(model.indexOf("module") + 6);
                model = model.substring(0, model.indexOf("{"));
                model = model.trim();
                result = model;
            } catch (Exception e) {
                result = "";
            }
        } else {
            result = "";
        }
        return result;
    }

}
