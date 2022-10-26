/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static java.util.Objects.requireNonNull;
import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;
import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    // Newline is treated separately, so it is not included here
    private static final CharMatcher NEED_QUOTE_MATCHER = CharMatcher.anyOf(" \t\r'\";{}");
    private static final CharMatcher DQUOT_MATCHER = CharMatcher.is('"');
    private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
    private static final ImmutableSet<StatementDefinition> QUOTE_MULTILINE_STATEMENTS = ImmutableSet.of(
        YangStmtMapping.CONTACT,
        YangStmtMapping.DESCRIPTION,
        YangStmtMapping.ERROR_MESSAGE,
        YangStmtMapping.ORGANIZATION,
        YangStmtMapping.REFERENCE);

    /*
     * https://tools.ietf.org/html/rfc6087#section-4.3:
     *            In general, it is suggested that substatements containing very common
     *            default values SHOULD NOT be present.  The following substatements
     *            are commonly used with the default value, which would make the module
     *            difficult to read if used everywhere they are allowed.
     */
    private static final ImmutableMap<StatementDefinition, String> DEFAULT_STATEMENTS =
            ImmutableMap.<StatementDefinition, String>builder()
            .put(YangStmtMapping.CONFIG, "true")
            .put(YangStmtMapping.MANDATORY, "true")
            .put(YangStmtMapping.MAX_ELEMENTS, "unbounded")
            .put(YangStmtMapping.MIN_ELEMENTS, "0")
            .put(YangStmtMapping.ORDERED_BY, "system")
            .put(YangStmtMapping.REQUIRE_INSTANCE, "true")
            .put(YangStmtMapping.STATUS, "current")
            .put(YangStmtMapping.YIN_ELEMENT, "false")
            .build();

    private static final String INDENT = "  ";
    private static final int INDENT_STRINGS_SIZE = 16;
    private static final String[] INDENT_STRINGS = new String[INDENT_STRINGS_SIZE];

    static {
        for (int i = 0; i < INDENT_STRINGS_SIZE; i++) {
            INDENT_STRINGS[i] = INDENT.repeat(i).intern();
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
    private final Set<StatementDefinition> ignoredStatements;
    private final StatementPrefixResolver resolver;
    private final boolean omitDefaultStatements;

    YangTextSnippetIterator(final DeclaredStatement<?> stmt, final StatementPrefixResolver resolver,
        final Set<StatementDefinition> ignoredStatements, final boolean omitDefaultStatements) {
        this.resolver = requireNonNull(resolver);
        this.ignoredStatements = requireNonNull(ignoredStatements);
        this.omitDefaultStatements = omitDefaultStatements;
        pushStatement(requireNonNull(stmt));
    }

    @Override
    protected @NonNull String computeNext() {
        // We may have some strings stashed, take one out, if that is the case
        final String nextString = strings.poll();
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
        final var def = stmt.statementDefinition();
        if (ignoredStatements.contains(def)) {
            return false;
        }

        final var children = stmt.declaredSubstatements();
        if (omitDefaultStatements && children.isEmpty()) {
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
        resolver.findPrefix(stmt).ifPresent(prefix -> {
            strings.add(prefix);
            strings.add(":");
        });
        strings.add(def.getStatementName().getLocalName());

        // Add argument, quoted and properly indented if need be
        addArgument(def, stmt.rawArgument());

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

    private void addArgument(final StatementDefinition def, final @Nullable String arg) {
        if (arg == null) {
            // No argument, nothing to do
        } else if (arg.isEmpty()) {
            // Argument is empty, quote an empty string.
            strings.add(" \"\"");
        } else if (QUOTE_MULTILINE_STATEMENTS.contains(def) || arg.indexOf('\n') != -1) {
            // Quote starting on next line.
            strings.add("\n");
            addIndent();
            strings.add(INDENT + '\"');

            final Iterator<String> it = NEWLINE_SPLITTER.split(DQUOT_MATCHER.replaceFrom(arg, "\\\"")).iterator();
            final String first = it.next();
            if (!first.isEmpty()) {
                strings.add(first);
            }

            while (it.hasNext()) {
                strings.add("\n");
                final String str = it.next();
                if (!str.isEmpty()) {
                    addIndent();
                    strings.add(INDENT + ' ');
                    strings.add(str);
                }
            }
            strings.add("\"");
        } else if (NEED_QUOTE_MATCHER.matchesAnyOf(arg) || arg.contains("//") || arg.contains("/*")
            || arg.contains("*/")) {
            // Quote on the same line.
            strings.add(" \"");
            strings.add(DQUOT_MATCHER.replaceFrom(arg, "\\\""));
            strings.add("\"");
        } else {
            // No quoting necessary.
            strings.add(" ");
            strings.add(arg);
        }
    }
}
