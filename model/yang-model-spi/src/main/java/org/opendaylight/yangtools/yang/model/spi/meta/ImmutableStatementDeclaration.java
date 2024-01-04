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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInFile;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * Reference of statement source present in textual source format. Utility implementation
 * of {@link StatementSourceReference} for textual sources, this is preferred {@link StatementSourceReference}
 * for implementations of YANG / YIN statement stream sources.
 *
 * <p>
 * To create source reference use one of this static factories:
 * <ul>
 *   <li>{@link #inText(String, int, int)} - provides most specific reference of statement location, this is most
 *       preferred since it provides most context to debug YANG model.</li>
 *   <li>{@link #inText(int, int)}- provides location in text without knowing the name of the text file.</li>
 *   <li>{@link #inFile(String)}- provides a source name.</li>
 * </ul>
 */
@NonNullByDefault
public abstract class ImmutableStatementDeclaration extends StatementDeclaration {
    private static class InTextInt extends StatementDeclaration.InText {
        private final int startLine;
        private final int startColumn;

        InTextInt(final int startLine, final int startColumn) {
            checkArgument(startLine > 0, "Invalid start line %s", startLine);
            checkArgument(startColumn > 0, "Invalid start column %s", startColumn);
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
        protected @Nullable String file() {
            return null;
        }
    }

    private static class InTextShort extends StatementDeclaration.InText  {
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

        @Override
        protected @Nullable String file() {
            return null;
        }
    }

    private static final class InTextFileInt extends InTextInt implements DeclarationInFile {
        private final String fileName;

        InTextFileInt(final int line, final int column, final String fileName) {
            super(line, column);
            checkArgument(!fileName.isEmpty(), "Invalid empty file name");
            this.fileName = fileName;
        }

        @Override
        public String fileName() {
            return fileName;
        }
    }

    private static final class InTextFileShort extends InTextShort implements DeclarationInFile {
        private final String fileName;

        InTextFileShort(final short line, final short column, final String fileName) {
            super(line, column);
            checkArgument(!fileName.isEmpty(), "Invalid empty file name");
            this.fileName = fileName;
        }

        @Override
        public String fileName() {
            return fileName;
        }
    }

    public static StatementDeclaration.InText inText(final int startLine, final int startColumn) {
        return canUseShort(startLine, startColumn) ? new InTextShort((short) startLine, (short) startColumn)
            : new InTextInt(startLine, startColumn);
    }

    public static StatementDeclaration.InText inText(final @Nullable String fileName, final int startLine,
            final int startColumn) {
        if (fileName == null) {
            return inText(startLine, startColumn);
        }

        return canUseShort(startLine, startColumn)
            ? new InTextFileShort((short) startLine, (short) startColumn, fileName)
                : new InTextFileInt(startLine, startColumn, fileName);
    }

    private static boolean canUseShort(final int startLine, final int startColumn) {
        checkArgument(startLine > 0, "Invalid start line %s", startLine);
        checkArgument(startColumn > 0, "Invalid start column %s", startColumn);
        return startLine <= 65535 && startColumn <= 65535;
    }
}
