/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import static org.opendaylight.yangtools.checkstyle.CheckLoggingUtil.LOGGER_VAR_NAME;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerVariableNameCheck extends Check {

    private static final String LOG_MESSAGE = "Logger name should be LOG.";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (CheckLoggingUtil.isAFieldVariable(aAST) && CheckLoggingUtil.isLoggerType(aAST)
                && !LOGGER_VAR_NAME.equals(CheckLoggingUtil.getVariableName(aAST))) {
            log(aAST.getLineNo(), LOG_MESSAGE);
        }
    }

}
