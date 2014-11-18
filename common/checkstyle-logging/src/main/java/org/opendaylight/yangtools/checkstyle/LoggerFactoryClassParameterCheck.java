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

public class LoggerFactoryClassParameterCheck extends Check {

    private static final String LOG_MESSAGE = "LoggerFactory.getLogger Class argument is incorrect.";
    private static final String METHOD_NAME = "LoggerFactory.getLogger";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        final String methodName = CheckLoggingUtil.getMethodName(aAST);
        if(methodName.equals(METHOD_NAME)) {
            final String className = CheckLoggingUtil.getClassName(aAST);
            final String parameter = aAST.findFirstToken(TokenTypes.ELIST).getFirstChild().getFirstChild().getFirstChild().getText();
            if(!parameter.equals(className)) {
                log(aAST.getLineNo(), LOG_MESSAGE);
            }
        }
    }

}
