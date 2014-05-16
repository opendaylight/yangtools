/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LogMessagePlaceholderCountCheck extends Check {

    private static final String LOG_MESSAGE = "Log message placeholders count is incorrect.";
    private static final String PLACEHOLDER = "{}";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        final String methodName = CheckLoggingUtil.getMethodName(aAST);
        if(CheckLoggingUtil.isLogMethod(methodName)) {
            final String logMessage = aAST.findFirstToken(TokenTypes.ELIST).getFirstChild().getFirstChild().getText();
            int placeholdersCount = placeholdersCount(logMessage);
            int argumentsCount = aAST.findFirstToken(TokenTypes.ELIST).getChildCount(TokenTypes.EXPR) - 1;
            final String lastArg = aAST.findFirstToken(TokenTypes.ELIST).getLastChild().getFirstChild().getText();
            if(hasCatchBlockParentWithArgument(lastArg, aAST)) {
                argumentsCount--;
            }
            if(placeholdersCount != argumentsCount) {
                log(aAST.getLineNo(), LOG_MESSAGE);
            }
        }
    }

    private int placeholdersCount(final String message) {
        return (message.length() - message.replace(PLACEHOLDER, "").length()) / PLACEHOLDER.length();
    }

    private boolean hasCatchBlockParentWithArgument(final String argumentName, final DetailAST aAST) {
        DetailAST parent = aAST.getParent();
        while(parent != null && parent.getType() != TokenTypes.LITERAL_CATCH) {
            parent = parent.getParent();
        }
        if(parent != null) {
            if(parent.findFirstToken(TokenTypes.PARAMETER_DEF) != null &&
                    parent.findFirstToken(TokenTypes.PARAMETER_DEF).findFirstToken(TokenTypes.IDENT).getText().equals(argumentName)) {
                return true;
            }
        }
        return false;
    }

}
