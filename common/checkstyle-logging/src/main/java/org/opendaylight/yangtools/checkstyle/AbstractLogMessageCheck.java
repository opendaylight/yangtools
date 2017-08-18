/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.Optional;

public abstract class AbstractLogMessageCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(final DetailAST ast) {
        String methodName = CheckLoggingUtil.getMethodName(ast);
        if (CheckLoggingUtil.isLogMethod(methodName)) {
            Optional<String> optLogMessage = getLogMessage(ast);
            optLogMessage.ifPresent(logMessage -> visitLogMessage(ast, logMessage));
        }
    }

    private static Optional<String> getLogMessage(DetailAST ast) {
        ast = ast.findFirstToken(TokenTypes.ELIST);
        if (ast != null) {
            ast = ast.getFirstChild();
            if (ast != null) {
                ast = ast.getFirstChild();
                if (ast != null) {
                    if (ast.getType() == TokenTypes.STRING_LITERAL) {
                        return Optional.ofNullable(ast.getText());
                    }
                }
            }
        }
        return Optional.empty();
    }

    protected abstract void visitLogMessage(DetailAST ast, String logMessage);
}
