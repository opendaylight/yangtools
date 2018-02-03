/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;
import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * This is an iterator over strings needed to assemble a YANG snippet.
 *
 * @author Robert Varga
 */
@NonNullByDefault({ PARAMETER, RETURN_TYPE })
final class YangTextSnippetIterator extends AbstractIterator<@NonNull String> {
    // https://tools.ietf.org/html/rfc7950#section-6.1.3
    //            An unquoted string is any sequence of characters that does not
    //            contain any space, tab, carriage return, or line feed characters, a
    //            single or double quote character, a semicolon (";"), braces ("{" or
    //            "}"), or comment sequences ("//", "/*", or "*/").
    private static final CharMatcher NEED_QUOTE_MATCHER = CharMatcher.anyOf(" \t\r\n'\";{}");

    private static final CharMatcher DQUOT_MATCHER = CharMatcher.is('"');
    private static final CharMatcher NEWLINE_MATCHER = CharMatcher.is('\n');
    private static final Splitter NEWLINE_SPLITTER = Splitter.on(NEWLINE_MATCHER);

    /*
     * https://tools.ietf.org/html/rfc6087#section-4.3:
     *            In general, it is suggested that substatements containing very common
     *            default values SHOULD NOT be present.  The following substatements
     *            are commonly used with the default value, which would make the module
     *            difficult to read if used everywhere they are allowed.
     */
    private static final Map<StatementDefinition, String> DEFAULT_STATEMENTS =
            ImmutableMap.<StatementDefinition, String>builder()
            .put(YangStmtMapping.CONFIG, "true")
            .put(YangStmtMapping.MANDATORY, "true")
            .put(YangStmtMapping.MAX_ELEMENTS, "unbounded")
            .put(YangStmtMapping.MIN_ELEMENTS, "0")
            .put(YangStmtMapping.ORDERED_BY, "system")
            .put(YangStmtMapping.STATUS, "current")
            .put(YangStmtMapping.YIN_ELEMENT, "false")
            .build();

    private static final String INDENT = "  ";
    private static final int INDENT_STRINGS_SIZE = 16;
    private static final String[] INDENT_STRINGS = new String[INDENT_STRINGS_SIZE];

    static {
        for (int i = 0; i < INDENT_STRINGS_SIZE; i++) {
            INDENT_STRINGS[i] = Strings.repeat(INDENT, i).intern();
        }
    }

    /*
     * We normally have up to 10 strings:
     *               <indent>
     *               <prefix>
     *               ":"
     *               <name>
     *               " \n"
     *               <indent>
     *               "\""
     *               <argument>
     *               "\""
     *               ";\n"
     *
     * But all of this is typically not used:
     * - statements usually do not have a prefix, saving two items
     * - arguments are not typically quoted, saving another two
     *
     * In case we get into a multi-line argument, we are already splitting strings, so the cost of growing
     * the queue is negligible
     */
    private final Queue<String> strings = new ArrayDeque<>(8);
    // Let's be modest, 16-level deep constructs are not exactly common.
    private final Deque<Iterator<? extends DeclaredStatement<?>>> stack = new ArrayDeque<>(8);
    private final Map<QNameModule, @NonNull String> namespaces;

    YangTextSnippetIterator(final DeclaredStatement<?> stmt, final Map<QNameModule, @NonNull String> namespaces) {
        this.namespaces = requireNonNull(namespaces);
        pushStatement(requireNonNull(stmt));
    }

    @Override
    protected @NonNull String computeNext() {
        // We may have some strings stashed, take one out, if that is the case
        final @Nullable String nextString = strings.poll();
        if (nextString != null) {
            return nextString;
        }

        final Iterator<? extends DeclaredStatement<?>> it = stack.peek();
        if (it == null) {
            endOfData();
            // Post-end of data, the user will never see this
            return "";
        }

        // Try to push next child
        while (it.hasNext()) {
            if (pushStatement(it.next())) {
                return strings.remove();
            }
        }

        // End of children, close the parent statement
        stack.pop();
        addIndent();
        strings.add("}\n");
        return strings.remove();
    }


    /**
     * Push a statement to the stack. A successfully-pushed statement results in strings not being empty.
     *
     * @param stmt Statement to push into strings
     * @return True if the statement was pushed. False if the statement was suppressed.
     */
    private boolean pushStatement(final DeclaredStatement<?> stmt) {
        final StatementDefinition def = stmt.statementDefinition();
        final Collection<? extends DeclaredStatement<?>> children = stmt.declaredSubstatements();
        if (children.isEmpty()) {
            // This statement does not have substatements, check if its value matches the declared default, like
            // "config true", "mandatory false", etc.
            final String suppressValue = DEFAULT_STATEMENTS.get(def);
            if (suppressValue != null && suppressValue.equals(stmt.rawArgument())) {
                return false;
            }
        }

        // New statement: push indent
        addIndent();

        // Add statement prefixed with namespace if needed
        final QName stmtName = def.getStatementName();
        addNamespace(stmtName.getModule());
        final String name = stmtName.getLocalName();
        strings.add(name);

        // Add argument, quoted and properly indented if need be
        addArgument(stmt.rawArgument());

        if (!children.isEmpty()) {
            // Open the statement and push child iterator
            strings.add(" {\n");
            stack.push(children.iterator());
        } else {
            // Close the statement
            strings.add(";\n");
        }

        return true;
    }

    private void addIndent() {
        int depth = stack.size();
        while (depth >= INDENT_STRINGS_SIZE) {
            strings.add(INDENT_STRINGS[INDENT_STRINGS_SIZE - 1]);
            depth -= INDENT_STRINGS_SIZE;
        }
        if (depth > 0) {
            strings.add(INDENT_STRINGS[depth]);
        }
    }

    private void addNamespace(final QNameModule namespace) {
        if (YangConstants.RFC6020_YIN_MODULE.equals(namespace)) {
            // Default namespace, no prefix needed
            return;
        }

        final @Nullable String prefix = namespaces.get(namespace);
        checkArgument(prefix != null, "Failed to find prefix for namespace %s", namespace);
        verify(prefix.isEmpty(), "Empty prefix for namespace %s", namespace);
        strings.add(prefix);
        strings.add(":");
    }

    private void addArgument(final @Nullable String arg) {
        if (arg == null) {
            // No argument, nothing to do
            return;
        }

        if (NEED_QUOTE_MATCHER.matchesNoneOf(arg) &&
                !(arg.contains("//") || arg.contains("/*") || arg.contains("*/"))) {
            // No need to escape or split
            strings.add(" ");
            strings.add(arg);
            return;
        }

        strings.add(" \n");
        addIndent();
        // javac is smart enough to merge this into a single string
        strings.add(INDENT + '\"');

        final String escaped = DQUOT_MATCHER.replaceFrom(arg, "\\\"");
        if (NEWLINE_MATCHER.matchesAnyOf(escaped)) {
            final Iterator<String> it = NEWLINE_SPLITTER.split(escaped).iterator();
            strings.add(it.next());

            while (it.hasNext()) {
                strings.add("\n");
                final String str = it.next();
                if (!str.isEmpty()) {
                    addIndent();
                    strings.add(str);
                }
            }
        } else {
            strings.add(escaped);
        }

        strings.add("\"");
    }
}
