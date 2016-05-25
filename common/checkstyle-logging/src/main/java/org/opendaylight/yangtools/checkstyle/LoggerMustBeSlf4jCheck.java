/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import static org.opendaylight.yangtools.checkstyle.CheckLoggingUtil.LOGGER_TYPE_FULL_NAME;
import static org.opendaylight.yangtools.checkstyle.CheckLoggingUtil.LOGGER_TYPE_NAME;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerMustBeSlf4jCheck extends AbstractCheck {

    private static final String LOG_MESSAGE = "Logger must be slf4j.";
    private static final String SLF4J = "slf4j";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF, TokenTypes.IMPORT};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (aAST.getType() == TokenTypes.VARIABLE_DEF) {
            if (CheckLoggingUtil.isAFieldVariable(aAST)) {
                final String typeName = CheckLoggingUtil.getTypeName(aAST);
                if (typeName.contains("." + LOGGER_TYPE_NAME) && !typeName.equals(LOGGER_TYPE_FULL_NAME)) {
                    log(aAST.getLineNo(), LOG_MESSAGE);
                }
            }
        } else if (aAST.getType() == TokenTypes.IMPORT) {
            final DetailAST typeToken = aAST.getFirstChild().findFirstToken(TokenTypes.IDENT);
            if (typeToken != null) {
                final String importType = typeToken.getText();
                if (CheckLoggingUtil.LOGGER_TYPE_NAME.equals(importType)) {
                    final String importIdent = aAST.getFirstChild().getFirstChild().getLastChild().getText();
                    if (!importIdent.equals(SLF4J)) {
                        log(aAST.getLineNo(), LOG_MESSAGE);
                    }
                }
            }
        }
    }
}
