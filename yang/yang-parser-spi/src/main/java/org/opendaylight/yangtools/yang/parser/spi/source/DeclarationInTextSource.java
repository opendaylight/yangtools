/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

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
 *   <li>{@link #atLine(String, int)}- provides source and line of statement location.</li>
 *   <li>{@link #inSource(String)} - least specific reference, should be used only if any of previous references are
 *       unable to create / derive from source.</li>
 * </ul>
 */
public abstract class DeclarationInTextSource implements StatementSourceReference {
    private static class InSource extends DeclarationInTextSource {
        InSource(final String sourceName) {
            super(sourceName);
        }
    }

    private static class AtLine extends InSource {
        private final int line;

        AtLine(final String sourceName, final int line) {
            super(sourceName);
            this.line = line;
        }

        @Override
        int hashCodeImpl() {
            return super.hashCodeImpl() * 31 + line;
        }

        @Override
        boolean equalsImpl(final DeclarationInTextSource obj) {
            return line == ((AtLine) obj).line && super.equalsImpl(obj);
        }

        @Override
        public String toString() {
            return super.toString() + ':' + line;
        }
    }

    private static final class AtPosition extends AtLine {
        private final int character;

        AtPosition(final String sourceName, final int line, final int character) {
            super(sourceName, line);
            this.character = character;
        }

        @Override
        int hashCodeImpl() {
            return super.hashCodeImpl() * 31 + character;
        }

        @Override
        boolean equalsImpl(final DeclarationInTextSource obj) {
            return character == ((AtPosition) obj).character && super.equalsImpl(obj);
        }

        @Override
        public String toString() {
            return super.toString() + ':' + character;
        }
    }

    private final String sourceName;

    DeclarationInTextSource(final String sourceName) {
        this.sourceName = sourceName;
    }

    public static @NonNull DeclarationInTextSource inSource(final String sourceName) {
        return new InSource(sourceName);
    }

    public static @NonNull DeclarationInTextSource atLine(final String sourceName, final int line) {
        return new AtLine(sourceName, line);
    }

    public static @NonNull DeclarationInTextSource atPosition(final String sourceName, final int line,
            final int position) {
        return new AtPosition(sourceName, line, position);
    }

    public final String getSourceName() {
        return sourceName;
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public final int hashCode() {
        return hashCodeImpl();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj
                || obj != null && getClass().equals(obj.getClass()) && equalsImpl((DeclarationInTextSource) obj);
    }

    @Override
    public String toString() {
        return sourceName == null ? "null" : sourceName;
    }

    int hashCodeImpl() {
        return Objects.hashCode(sourceName);
    }

    boolean equalsImpl(final DeclarationInTextSource obj) {
        return Objects.equals(sourceName, obj.sourceName);
    }
}
