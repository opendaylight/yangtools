/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import static org.opendaylight.yangtools.checkstyle.CheckLoggingUtil.isAFieldVariable;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerDeclarationsCountCheck extends Check {

    private static final String LOG_MESSAGE = "Logger might be declared only once.";
    private String prevClassName = "";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (CheckLoggingUtil.isLoggerType(ast) && isAFieldVariable(ast)) {
            final String className = CheckLoggingUtil.getClassName(ast);
            if (this.prevClassName.equals(className)) {
                log(ast.getLineNo(), LOG_MESSAGE);
            }
            this.prevClassName = className;
        }
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        super.finishTree(rootAST);
        this.prevClassName = "";
    }

}
