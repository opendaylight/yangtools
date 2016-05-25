/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtils;

public final class CheckLoggingUtil {

    public static final String LOGGER_TYPE_NAME = Logger.class.getSimpleName();
    public static final String LOGGER_TYPE_FULL_NAME = Logger.class.getName();
    public static final String LOGGER_VAR_NAME = "LOG";
    private static final List<String> LOG_METHODS =
            Lists.newArrayList("LOG.debug", "LOG.info", "LOG.error", "LOG.warn", "LOG.trace");

    private CheckLoggingUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated!");
    }

    public static String getTypeName(final DetailAST ast) {
        final FullIdent ident = CheckUtils.createFullType(ast.findFirstToken(TokenTypes.TYPE));
        return ident.getText();
    }

    public static boolean isLoggerType(final DetailAST ast) {
        final String typeName = getTypeName(ast);
        return typeName.equals(LOGGER_TYPE_FULL_NAME) || typeName.equals(LOGGER_TYPE_NAME);
    }

    public static String getVariableName(final DetailAST ast) {
        DetailAST identifier = ast.findFirstToken(TokenTypes.IDENT);
        return identifier.getText();
    }

    public static boolean isAFieldVariable(final DetailAST ast) {
        return ast.getParent().getType() == TokenTypes.OBJBLOCK;
    }

    public static String getMethodName(final DetailAST ast) {
        if (ast.getFirstChild().getLastChild() != null) {
            return ast.getFirstChild().getFirstChild().getText() + "." + ast.getFirstChild().getLastChild().getText();
        }
        return ast.getFirstChild().getText();
    }

    public static boolean isLogMethod(final String methodName) {
        return LOG_METHODS.contains(methodName);
    }

    public static String getClassName(final DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent.getType() != TokenTypes.CLASS_DEF && parent.getType() != TokenTypes.ENUM_DEF) {
            parent = parent.getParent();
        }
        return parent.findFirstToken(TokenTypes.IDENT).getText();
    }

}
