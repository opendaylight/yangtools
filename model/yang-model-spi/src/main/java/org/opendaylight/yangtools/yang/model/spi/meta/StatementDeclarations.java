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
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInFile;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInText;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * Factory for creating default {@link StatementDeclaration} implementations. This is preferred source of
 * {@link StatementSourceReference} for implementations of YANG / YIN statement stream sources.
 *
 * <p>
 * To create source reference use one of this static factories:
 * <ul>
 *   <li>{@link #inText(String, int, int)} - provides most specific reference of statement location, this is most
 *       preferred since it provides most context to debug YANG model.</li>
 *   <li>{@link #inText(int, int)}- provides location in text without knowing the name of the text file.</li>
 * </ul>
 */
public final class StatementDeclarations {
    private static class InTextInt extends StatementDeclaration.InText implements DeclarationInText {
        private final int startLine;
        private final int startColumn;

        InTextInt(final int startLine, final int startColumn) {
            this.startLine = startLine;
            this.startColumn = startColumn;
        }

        @Override
        public int startLine() {
            return startLine;
        }

        @Override
        public int startColumn() {
            return startColumn;
        }
    }

    private static class InTextShort extends StatementDeclaration.InText implements DeclarationInText {
        private final short startLine;
        private final short startColumn;

        InTextShort(final short startLine, final short startColumn) {
            this.startLine = startLine;
            this.startColumn = startColumn;
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

    private static final class InTextFileInt extends StatementDeclaration.InTextFile implements DeclarationInFile {
        private final @NonNull String fileName;
        private final int startLine;
        private final int startColumn;

        InTextFileInt(final String fileName, final int startLine, final int startColumn) {
            this.fileName = requireNonNull(fileName);
            this.startLine = startLine;
            this.startColumn = startColumn;
        }

        @Override
        public int startLine() {
            return startLine;
        }

        @Override
        public int startColumn() {
            return startColumn;
        }

        @Override
        public String fileName() {
            return fileName;
        }
    }

    private static final class InTextFileShort extends StatementDeclaration.InTextFile implements DeclarationInFile {
        private final @NonNull String fileName;
        private final short startLine;
        private final short startColumn;

        InTextFileShort(final String fileName, final short startLine, final short startColumn) {
            this.fileName = requireNonNull(fileName);
            this.startLine = startLine;
            this.startColumn = startColumn;
        }

        @Override
        public int startLine() {
            return Short.toUnsignedInt(startLine);
        }

        @Override
        public int startColumn() {
            return Short.toUnsignedInt(startColumn);
        }

        @Override
        public String fileName() {
            return fileName;
        }
    }

    public static StatementDeclaration.@NonNull InText inText(final int startLine, final int startColumn) {
        return canUseShort(startLine, startColumn) ? new InTextShort((short) startLine, (short) startColumn)
            : new InTextInt(startLine, startColumn);
    }

    public static StatementDeclaration.@NonNull InText inText(final int startLine, final int startColumn,
            final @Nullable String fileName) {
        if (fileName == null) {
            return inText(startLine, startColumn);
        }
        checkArgument(!fileName.isEmpty(), "Invalid empty file name");
        return canUseShort(startLine, startColumn)
            ? new InTextFileShort(fileName, (short) startLine, (short) startColumn)
                : new InTextFileInt(fileName, startLine, startColumn);
    }

    private static boolean canUseShort(final int startLine, final int startColumn) {
        checkArgument(startLine > 0, "Invalid start line %s", startLine);
        checkArgument(startColumn > 0, "Invalid start column %s", startColumn);
        return startLine <= 65535 && startColumn <= 65535;
    }
}
