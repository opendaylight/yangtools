/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import static org.opendaylight.yangtools.checkstyle.CheckLoggingUtil.isAFieldVariable;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class LoggerDeclarationsCountCheck extends AbstractCheck {

    private static final String LOG_MESSAGE = "Logger might be declared only once.";
    private String prevClassName = "";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (CheckLoggingUtil.isLoggerType(aAST) && isAFieldVariable(aAST)) {
            final String className = CheckLoggingUtil.getClassName(aAST);
            if(this.prevClassName.equals(className)) {
                log(aAST.getLineNo(), LOG_MESSAGE);
            }
            this.prevClassName = className;
        }
    }

    @Override
    public void finishTree(DetailAST aRootAST) {
        super.finishTree(aRootAST);
        this.prevClassName = "";
    }

}
