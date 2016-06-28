/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerFactoryClassParameterCheck extends AbstractCheck {

    private static final String LOG_MESSAGE = "LoggerFactory.getLogger Class argument is incorrect.";
    private static final String METHOD_NAME = "LoggerFactory.getLogger";

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.METHOD_CALL };
    }

    @Override
    public void visitToken(DetailAST ast) {
        final String methodName = CheckLoggingUtil.getMethodName(ast);
        if (methodName.equals(METHOD_NAME)) {
            final String className = CheckLoggingUtil.getClassName(ast);
            DetailAST findFirstToken = ast.findFirstToken(TokenTypes.ELIST);
            if (findFirstToken == null) {
                logError(ast, className);
                return;
            }
            DetailAST childToken = findFirstToken.getFirstChild();
            if (childToken == null) {
                logError(ast, className);
                return;
            }
            childToken = childToken.getFirstChild();
            if (childToken == null) {
                logError(ast, className);
                return;
            }
            childToken = childToken.getFirstChild();
            if (childToken == null) {
                logError(ast, className);
                return;
            }
            final String token = childToken.getText();
            if (!token.equals(className)) {
                log(ast.getLineNo(), LOG_MESSAGE);
            }
        }
    }

    protected void logError(DetailAST ast, String className) {
        log(ast.getLineNo(), String.format("Invalid parameter in \"getLogger\" method call in class: %s", className));
    }
}
