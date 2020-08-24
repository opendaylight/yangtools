/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

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
public abstract sealed class IRStatement extends AbstractIRObject {
    static final class Z22 extends IRStatement {
        private final short startLine;
        private final short startColumn;

        Z22(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn) {
            super(keyword, argument);
            this.startLine = (short) startLine;
            this.startColumn = (short) startColumn;
        }

        @Override
        public int startLine() {
            return Short.toUnsignedInt(startLine);
        }

        @Override
        public int startColumn() {
            return Short.toUnsignedInt(startColumn);
        }
    }

    static final class Z31 extends IRStatement {
        private final int value;

        Z31(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn) {
            this(keyword, argument, startLine << 8 | startColumn & 0xFF);
        }

        Z31(final IRKeyword keyword, final IRArgument argument, final int value) {
            super(keyword, argument);
            this.value = value;
        }

        @Override
        public int startLine() {
            return value >>> 8;
        }

        @Override
        public int startColumn() {
            return value & 0xFF;
        }

        int value() {
            return value;
        }
    }

    static sealed class Z44 extends IRStatement permits O44, L44 {
        private final int startLine;
        private final int startColumn;

        Z44(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn) {
            super(keyword, argument);
            this.startLine = startLine;
            this.startColumn = startColumn;
        }

        @Override
        public final int startLine() {
            return startLine;
        }

        @Override
        public final int startColumn() {
            return startColumn;
        }
    }

    static final class O44 extends Z44 {
        private final @NonNull IRStatement statement;

        O44(final IRKeyword keyword, final IRArgument argument, final IRStatement statement, final int startLine,
                final int startColumn) {
            super(keyword, argument, startLine, startColumn);
            this.statement = requireNonNull(statement);
        }

        @Override
        public ImmutableList<IRStatement> statements() {
            return ImmutableList.of(statement);
        }
    }

    static final class L44 extends Z44 {
        private final @NonNull ImmutableList<IRStatement> statements;

        L44(final IRKeyword keyword, final IRArgument argument, final ImmutableList<IRStatement> statements,
                final int startLine, final int startColumn) {
            super(keyword, argument, startLine, startColumn);
            this.statements = requireNonNull(statements);
        }

        @Override
        public ImmutableList<IRStatement> statements() {
            return statements;
        }
    }

    private final @NonNull IRKeyword keyword;
    private final IRArgument argument;

    IRStatement(final IRKeyword keyword, final IRArgument argument) {
        this.keyword = requireNonNull(keyword);
        this.argument = argument;
    }

    public static @NonNull IRStatement of(final IRKeyword keyword, final IRArgument argument, final int line,
            final int column, final ImmutableList<IRStatement> statements) {
        return switch (statements.size()) {
            case 0 -> {
                if (line >= 0 && column >= 0) {
                    if (line <= 65535 && column <= 65535) {
                        yield new Z22(keyword, argument, line, column);
                    }
                    if (line <= 16777215 && column <= 255) {
                        yield new Z31(keyword, argument, line, column);
                    }
                }
                yield new Z44(keyword, argument, line, column);
            }
            case 1 -> new O44(keyword, argument, statements.get(0), line, column);
            default -> new L44(keyword, argument, statements, line, column);
        };
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

        final var statements = statements();
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
        return obj == this || obj instanceof IRStatement other && keyword.equals(other.keyword)
            && Objects.equals(argument, other.argument) && startLine() == other.startLine()
            && startColumn() == other.startColumn() && statements().equals(other.statements());
    }
}
