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

public abstract class AbstractLogMessageCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST ast) {
        String methodName = CheckLoggingUtil.getMethodName(ast);
        if (CheckLoggingUtil.isLogMethod(methodName)) {
            String logMessage = ast.findFirstToken(TokenTypes.ELIST).getFirstChild().getFirstChild().getText();
            visitLogMessage(ast, logMessage);
        }
    }

    protected abstract void visitLogMessage(DetailAST ast, String logMessage);
}
