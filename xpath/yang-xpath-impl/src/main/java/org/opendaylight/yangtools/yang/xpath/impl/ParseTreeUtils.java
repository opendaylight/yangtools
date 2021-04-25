/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static com.google.common.base.Verify.verify;

import com.google.common.base.VerifyException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Utility methods for dealing with {@link ParseTree}s.
 */
final class ParseTreeUtils {
    private ParseTreeUtils() {

    }

    static <T extends ParseTree> T getChild(final ParseTree parent, final Class<T> type, final int offset) {
        return verifyTree(type, parent.getChild(offset));
    }

    static void verifyChildCount(final ParseTree tree, final int expected) {
        if (tree.getChildCount() != expected) {
            throw illegalShape(tree);
        }
    }

    static int verifyAtLeastChildren(final ParseTree tree, final int expected) {
        final int count = tree.getChildCount();
        if (count < expected) {
            throw illegalShape(tree);
        }
        return count;
    }

    static TerminalNode verifyTerminal(final ParseTree tree) {
        if (tree instanceof TerminalNode) {
            return (TerminalNode) tree;
        }
        throw new VerifyException(String.format("'%s' is not a terminal node", tree.getText()));
    }

    static Token verifyToken(final ParseTree parent, final int offset, final int expected) {
        final TerminalNode node = verifyTerminal(parent.getChild(offset));
        final Token ret = node.getSymbol();
        final int type = ret.getType();
        verify(type == expected, "Item %s has type %s, expected %s", node, type, expected);
        return ret;
    }

    static <T extends ParseTree> T verifyTree(final Class<T> type, final ParseTree tree) {
        if (type.isInstance(tree)) {
            return type.cast(tree);
        }
        throw new VerifyException(String.format("'%s' does not have expected type %s", tree.getText(), type));
    }

    static VerifyException illegalShape(final ParseTree tree) {
        return new VerifyException(String.format("Invalid parser shape of '%s'", tree.getText()));
    }
}
