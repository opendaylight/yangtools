/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link StatementSourceReference} which acts as its own {@link DeclarationReference}.
 */
@NonNullByDefault
public abstract class StatementDeclaration extends StatementSourceReference implements DeclarationReference {
    /**
     * A {@link StatementDeclaration} which acts as its own {@link DeclarationInText}.
     */
    public abstract static class InText extends StatementDeclaration implements DeclarationInText {
        @Override
        protected final int line() {
            return startLine();
        }

        @Override
        protected final int column() {
            return startColumn();
        }
    }
    /**
     * A {@link StatementDeclaration.InText} which acts as its own {@link DeclarationInFile}.
     */
    public abstract static class InTextFile extends InText implements DeclarationInFile {
        @Override
        protected final @NonNull String file() {
            return fileName();
        }
    }

    @Override
    public final StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public final @NonNull DeclarationReference declarationReference() {
        return this;
    }

    @Override
    public final String toHumanReadable() {
        final var sb = new StringBuilder();
        final var file = file();
        sb.append(file != null ? file : "<UNKNOWN>");
        final var line = line();
        if (line > 0) {
            sb.append(':').append(line);
        }
        final var column = column();
        if (column > 0) {
            sb.append(':').append(column);
        }
        return sb.toString();
    }

    @Override
    public final String toString() {
        return toHumanReadable();
    }

    protected abstract @Nullable String file();

    protected abstract int line();

    protected abstract int column();
}
