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
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
        keyword.toYangFragment(sb);
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

    @Override
    public final int hashCode() {
        return Objects.hash(keyword, argument, statements()) ^ startLine() ^ startColumn();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IRStatement)) {
            return false;
        }
        final IRStatement other = (IRStatement) obj;
        return keyword.equals(other.keyword) && Objects.equals(argument, other.argument)
                && startLine() == other.startLine() && startColumn() == other.startColumn()
                && statements().equals(other.statements());
    }
}
