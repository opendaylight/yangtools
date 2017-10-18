/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import static org.opendaylight.yangtools.checkstyle.CheckLoggingUtil.LOGGER_VAR_NAME;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerVariableNameCheck extends AbstractCheck {

    private static final String LOG_MESSAGE = "Logger name should be LOG.";
    private static final int[] TOKENS = { TokenTypes.VARIABLE_DEF };

    @Override
    public int[] getDefaultTokens() {
        return TOKENS;
    }

    @Override
    public int[] getAcceptableTokens() {
        return TOKENS;
    }

    @Override
    public int[] getRequiredTokens() {
        return TOKENS;
    }

    @Override
    public void visitToken(final DetailAST ast) {
        if (CheckLoggingUtil.isAFieldVariable(ast) && CheckLoggingUtil.isLoggerType(ast)
                && !LOGGER_VAR_NAME.equals(CheckLoggingUtil.getVariableName(ast))) {
            log(ast.getLineNo(), LOG_MESSAGE);
        }
    }
}
