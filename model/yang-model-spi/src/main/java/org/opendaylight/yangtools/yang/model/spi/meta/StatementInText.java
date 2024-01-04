/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInFile;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInText;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * Reference of statement source present in textual source format. Utility implementation
 * of {@link StatementSourceReference} for textual sources, this is preferred {@link StatementSourceReference}
 * for implementations of YANG / YIN statement stream sources.
 *
 * <p>
 * To create source reference use one of this static factories:
 * <ul>
 *   <li>{@link #atPosition(String, int, int)} - provides most specific reference of statement location, this is most
 *       preferred since it provides most context to debug YANG model.</li>
 *   <li>{@link #atPosition(int, int)}- provides location in text without knowing the name of the text file.</li>
 *   <li>{@link #inFile(String)}- provides a source name.</li>
 * </ul>
 */
public abstract class StatementInText extends StatementSourceReference implements DeclarationReference {
    private static final class InFile extends StatementInText implements DeclarationInFile {
        InFile(final String fileName) {
            super(fileName, -1, -1);
            checkArgument(!fileName.isEmpty(), "Invalid empty file name");
        }

        @Override
        public String fileName() {
            return verifyNotNull(file());
        }
    }

    private static class InText extends StatementInText implements DeclarationInText {
        InText(final String file, final int line, final int column) {
            super(file, line, column);
            checkArgument(line > 0, "Invalid start line %s", line);
            checkArgument(column > 0, "Invalid start column %s", column);
        }

        @Override
        public int startLine() {
            return line();
        }

        @Override
        public int startColumn() {
            return column();
        }
    }

    private static final class InTextFile extends InText implements DeclarationInFile {
        InTextFile(final String fileName, final int line, final int column) {
            super(fileName, line, column);
            checkArgument(!fileName.isEmpty(), "Invalid empty file name");
        }

        @Override
        public String fileName() {
            return verifyNotNull(file());
        }
    }

    private final String file;
    private final int line;
    private final int column;

    StatementInText(final String file, final int line, final int column) {
        this.file = file;
        this.line = line;
        this.column = column;
    }

    public static @NonNull StatementInText atPosition(final int line, final int column) {
        return new InText(null, line, column);
    }

    public static @NonNull StatementInText atPosition(final @Nullable String fileName, final int line,
            final int column) {
        return fileName == null ? atPosition(line, column) : new InTextFile(fileName, line, column);
    }

    public static @NonNull StatementInText inFile(final @NonNull String fileName) {
        return new InFile(fileName);
    }

    @Override
    public final StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public final DeclarationReference declarationReference() {
        return this;
    }

    @Override
    public final String toHumanReadable() {
        final var sb = new StringBuilder();
        sb.append(file != null ? file : "<UNKNOWN>");
        if (line > 0) {
            sb.append(':').append(line);
        }
        if (column > 0) {
            sb.append(':').append(column);
        }
        return sb.toString();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(file(), line(), column());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        final StatementInText other = (StatementInText) obj;
        return line == other.line && column == other.column && Objects.equals(file, other.file);
    }

    @Override
    public final String toString() {
        return toHumanReadable();
    }

    final String file() {
        return file;
    }

    final int line() {
        return line;
    }

    final int column() {
        return column;
    }
}
