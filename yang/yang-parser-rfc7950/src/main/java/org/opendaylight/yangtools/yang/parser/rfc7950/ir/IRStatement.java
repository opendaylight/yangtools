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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;

@Beta
public abstract class IRStatement implements Immutable {
    private final @NonNull IRKeyword keyword;
    private final IRArgument argument;

    IRStatement(final IRKeyword keyword, final IRArgument argument) {
        this.keyword = requireNonNull(keyword);
        this.argument = argument;
    }

    public static @NonNull IRStatement forContext(final StatementContext context) {
        return new StatementFactory().createStatement(context);
    }

    public final @NonNull IRKeyword keyword() {
        return keyword;
    }

    public final @Nullable IRArgument argument() {
        return argument;
    }

    public @NonNull ImmutableList<IRStatement> statements() {
        return ImmutableList.of();
    }

    public abstract int startLine();

    public abstract int startColumn();

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder().append(keyword);
        if (argument != null) {
            sb.append(' ').append(argument);
        }

        final List<IRStatement> statements = statements();
        if (!statements.isEmpty()) {
            sb.append(" {\n");
            for (IRStatement stmt : statements) {
                sb.append(stmt).append('\n');
            }
            sb.append("}");
        } else {
            sb.append(';');
        }

        return sb.toString();
    }
}
