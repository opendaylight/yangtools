/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;

/**
 * A single YANG statement in its raw string form. A statement is composed of:
 * <ul>
 *   <li>a mandatory keyword, modeled as {@link IRKeyword}</li>
 *   <li>an optional argument, modeled as {@link IRArgument}</li>
 *   <li>zero or more nested statements</li>
 * </ul>
 */
@Beta
public abstract class IRStatement extends AbstractIRObject {
    private final @NonNull IRKeyword keyword;
    private final IRArgument argument;

    IRStatement(final IRKeyword keyword, final IRArgument argument) {
        this.keyword = requireNonNull(keyword);
        this.argument = argument;
    }

    /**
     * Create an {@link IRStatement} from a parsed {@link StatementContext}.
     *
     * @param context ANTLR statement context
     * @return A new IRStatement
     * @throws NullPointerException if {@code context} is null
     */
    public static @NonNull IRStatement forContext(final StatementContext context) {
        return new StatementFactory().createStatement(context);
    }

    /**
     * Return this statement's keyword.
     *
     * @return This statement's keyword.
     */
    public final @NonNull IRKeyword keyword() {
        return keyword;
    }

    /**
     * Return this statement's argument, if it is present.
     *
     * @return This statement's argument, or null if this statement does not have an argument
     */
    public final @Nullable IRArgument argument() {
        return argument;
    }

    /**
     * Return this statement's substatements.
     *
     * @return This statement's substatements.
     */
    public @NonNull List<? extends IRStatement> statements() {
        return ImmutableList.of();
    }

    /**
     * Return the line number on which this statement's keyword has its first character, counting from <b>1</b>. This
     * information is used only for diagnostic purposes.
     *
     * @return Line number where this statement started in the source code.
     */
    public abstract int startLine();

    /**
     * Return the column number on which this statement's keyword has its first character, counting from <b>0</b>. This
     * information is used only for diagnostic purposes.
     *
     * @return Column number where this statement started in the source code.
     */
    public abstract int startColumn();

    @Override
    final StringBuilder toYangFragment(final StringBuilder sb) {
        sb.append(keyword);
        if (argument != null) {
            argument.toYangFragment(sb.append(' '));
        }

        final List<? extends IRStatement> statements = statements();
        if (statements.isEmpty()) {
            return sb.append(';');
        }

        sb.append(" {\n");
        for (IRStatement stmt : statements) {
            stmt.toYangFragment(sb).append('\n');
        }
        return sb.append('}');
    }
}
