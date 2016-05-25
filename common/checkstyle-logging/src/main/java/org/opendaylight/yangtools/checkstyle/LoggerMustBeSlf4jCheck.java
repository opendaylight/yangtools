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

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerMustBeSlf4jCheck extends Check {

    private static final String LOG_MESSAGE = "Logger must be slf4j.";
    private static final String SLF4J = "slf4j";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF, TokenTypes.IMPORT};
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.VARIABLE_DEF) {
            if (CheckLoggingUtil.isAFieldVariable(ast)) {
                final String typeName = CheckLoggingUtil.getTypeName(ast);
                if (typeName.contains("." + LOGGER_TYPE_NAME) && !typeName.equals(LOGGER_TYPE_FULL_NAME)) {
                    log(ast.getLineNo(), LOG_MESSAGE);
                }
            }
        } else if (ast.getType() == TokenTypes.IMPORT) {
            final DetailAST typeToken = ast.getFirstChild().findFirstToken(TokenTypes.IDENT);
            if (typeToken != null) {
                final String importType = typeToken.getText();
                if (CheckLoggingUtil.LOGGER_TYPE_NAME.equals(importType)) {
                    final String importIdent = ast.getFirstChild().getFirstChild().getLastChild().getText();
                    if (!importIdent.equals(SLF4J)) {
                        log(ast.getLineNo(), LOG_MESSAGE);
                    }
                }
            }
        }
    }
}
