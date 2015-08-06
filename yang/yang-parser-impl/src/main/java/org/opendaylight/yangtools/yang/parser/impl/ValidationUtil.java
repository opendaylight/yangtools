/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.base.Splitter;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Submodule_stmtContext;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

/**
 * Validation utilities
 */
final class ValidationUtil {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');

    /**
     * It isn't desirable to create instance of this class
     */
    private ValidationUtil() {
    }

    static void ex(final String message) {
        throw new YangValidationException(message);
    }

    static Set<String> getDuplicates(final Iterable<String> keyList) {
        Set<String> all = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for (String key : keyList) {
            if (!all.add(key)) {
                duplicates.add(key);
            }
        }
        return duplicates;
    }

    static Iterable<String> listKeysFromId(final String keys) {
        return SPACE_SPLITTER.split(keys);
    }

    static String getRootParentName(final ParseTree ctx) {
        ParseTree root = getRootParent(ctx);
        return ValidationUtil.getName(root);
    }

    private static ParseTree getRootParent(final ParseTree ctx) {
        ParseTree root = ctx;
        while (root.getParent() != null) {
            if (root.getClass().equals(Module_stmtContext.class) || root.getClass().equals(Submodule_stmtContext.class)) {
                break;
            }
            root = root.getParent();
        }
        return root;
    }

    static String getName(final ParseTree child) {
        return ParserListenerUtils.stringFromNode(child);
    }

    static String f(final String base, final Object... args) {
        return String.format(base, args);
    }

    /**
     * Get simple name from statement class e.g. Module from Module_stmt_context
     */
    static String getSimpleStatementName(final Class<? extends ParseTree> typeOfStatement) {

        String className = typeOfStatement.getSimpleName();
        int lastIndexOf = className.indexOf('$');
        className = lastIndexOf == -1 ? className : className.substring(lastIndexOf + 1);
        int indexOfStmt = className.indexOf("_stmt");
        int index = indexOfStmt == -1 ? className.indexOf("_arg") : indexOfStmt;
        return className.substring(0, index).replace('_', '-');
    }

    static int countPresentChildrenOfType(final ParseTree parent, final Set<Class<? extends ParseTree>> expectedChildTypes) {
        int foundChildrenOfType = 0;

        for (Class<? extends ParseTree> type : expectedChildTypes) {
            foundChildrenOfType += countPresentChildrenOfType(parent, type);
        }
        return foundChildrenOfType;
    }

    static int countPresentChildrenOfType(final ParseTree parent, final Class<? extends ParseTree> expectedChildType) {
        int foundChildrenOfType = 0;

        for (int i = 0; i < parent.getChildCount(); i++) {
            ParseTree child = parent.getChild(i);
            if (expectedChildType.isInstance(child)) {
                foundChildrenOfType++;
            }
        }
        return foundChildrenOfType;
    }

}
