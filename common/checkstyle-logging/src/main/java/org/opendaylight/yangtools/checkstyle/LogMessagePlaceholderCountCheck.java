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
    private static final String EXCEPTION_TYPE = "Exception";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        final String methodName = CheckLoggingUtil.getMethodName(aAST);
        if (CheckLoggingUtil.isLogMethod(methodName)) {
            final String logMessage = aAST.findFirstToken(TokenTypes.ELIST).getFirstChild().getFirstChild().getText();
            int placeholdersCount = placeholdersCount(logMessage);
            int argumentsCount = aAST.findFirstToken(TokenTypes.ELIST).getChildCount(TokenTypes.EXPR) - 1;
            final String lastArg = aAST.findFirstToken(TokenTypes.ELIST).getLastChild().getFirstChild().getText();
            if (hasCatchBlockParentWithArgument(lastArg, aAST) || hasMethodDefinitionWithExceptionArgument(lastArg,
                    aAST)) {
                argumentsCount--;
            }
            if (placeholdersCount > argumentsCount) {
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
        if (parent != null && parent.findFirstToken(TokenTypes.PARAMETER_DEF) != null &&
                parent.findFirstToken(TokenTypes.PARAMETER_DEF).findFirstToken(TokenTypes.IDENT).getText().equals
                        (argumentName)) {
            return true;
        }
        return false;
    }

    private boolean hasMethodDefinitionWithExceptionArgument(final String argumentName, final DetailAST aAST) {
        DetailAST parent = aAST.getParent();
        while(parent != null && parent.getType() != TokenTypes.METHOD_DEF) {
            parent = parent.getParent();
        }
        if (parent != null && parent.findFirstToken(TokenTypes.PARAMETERS).findFirstToken(TokenTypes.PARAMETER_DEF)
                != null) {
            DetailAST paramDef = parent.findFirstToken(TokenTypes.PARAMETERS).getFirstChild();
            while(paramDef != null) {
                if (paramDef.getType() == TokenTypes.PARAMETER_DEF) {
                    final String paramName = paramDef.findFirstToken(TokenTypes.IDENT).getText();
                    if (paramName.equals(argumentName) && isExceptionType(paramDef)) {
                        return true;
                    }
                }
                paramDef = paramDef.getNextSibling();
            }
        }
        return false;
    }

    private boolean isExceptionType(final DetailAST parameterDef) {
        if (parameterDef != null) {
            final DetailAST type = parameterDef.findFirstToken(TokenTypes.TYPE);
            if (type != null && type.findFirstToken(TokenTypes.IDENT) != null) {
                final String argumentType = type.findFirstToken(TokenTypes.IDENT).getText();
                if (argumentType.contains(EXCEPTION_TYPE)) {
                    return true;
                }
            }
        }
        return false;
    }

}
