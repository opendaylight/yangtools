/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

/**
 *
 * Reference of statement source present in textual source format
 *
 * Utility implementation of {@link StatementSourceReference} for textual sources,
 * this is prefered {@link StatementSourceReference} for implementations
 * of YANG / YIN statement stream sources.
 *
 *
 *  To create source reference use one of this static factories:
 *  <ul>
 *  <li>{@link #atPosition(String, int, int)} - provides most specific reference of statement location,
 *  this is most prefered since it provides most context to debug YANG model.
 *  </li>
 *  <li>{@link #atLine(String, int)}- provides source and line of statement location.
 *  </li>
 *  <li>{@link #inSource(String)} - least specific reference, should be used only if any of previous
 *  references are unable to create / derive from source.
 *  </li>
 *  </ul>
 *
 */
public abstract class DeclarationInTextSource implements StatementSourceReference {

    private final String sourceName;

    DeclarationInTextSource(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceName() {
        return sourceName;
    }

    @Override
    public StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public abstract String toString();

    public static DeclarationInTextSource inSource(String sourceName) {
        return new InSource(sourceName);
    }

    public static DeclarationInTextSource atLine(String sourceName, int line) {
        return new AtLine(sourceName, line);
    }

    public static DeclarationInTextSource atPosition(String sourceName, int line, int position) {
        return new AtPosition(sourceName, line,position);
    }

    private static class InSource extends DeclarationInTextSource {

        InSource(String sourceName) {
            super(sourceName);
        }

        @Override
        public String toString() {
            return getSourceName();
        }

    }

    private static class AtLine extends InSource {

        private final int line;

        AtLine(String sourceName, int line) {
            super(sourceName);
            this.line = line;
        }

        @Override
        public String toString() {
            return String.format("%s:%d", getSourceName(),line);
        }

        public int getLine() {
            return line;
        }

    }

    private static class AtPosition extends AtLine {

        private final int character;

        AtPosition(String sourceName, int line, int character) {
            super(sourceName, line);
            this.character = character;
        }

        @Override
        public String toString() {
            return String.format("%s:%d:%d", getSourceName(),getLine(),character);
        }

    }

}
