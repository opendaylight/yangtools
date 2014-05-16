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

public class LoggerVariableModifiersCheck extends Check {

    private static final String LOG_MESSAGE = "Logger must be declared as private static final.";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (CheckLoggingUtil.itsAFieldVariable(aAST) && CheckLoggingUtil.isLoggerType(aAST) && !hasPrivatStaticFinalModifier(aAST)) {
            log(aAST.getLineNo(), LOG_MESSAGE);
        }
    }

    private boolean hasPrivatStaticFinalModifier(DetailAST aAST) {
        DetailAST modifiers = aAST.findFirstToken(TokenTypes.MODIFIERS);
        if(modifiers != null) {
            if(modifiers.branchContains(TokenTypes.LITERAL_PRIVATE) && modifiers.branchContains(TokenTypes.LITERAL_STATIC)
                    && modifiers.branchContains(TokenTypes.FINAL)) {
                return true;
            }
        }
        return false;
    }

}
