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

    private final String sourceText;
    private final String sourceName;

    DeclarationInTextSource(String sourceName, String sourceText) {
        this.sourceName = sourceName;
        this.sourceText = sourceText;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceText() {
        return sourceText;
    }

    @Override
    public StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public abstract String toString();

    public static final DeclarationInTextSource inSource(String sourceName, String sourceText) {
        return new InSource(sourceName, sourceText);
    }

    public static final DeclarationInTextSource atLine(String sourceName, String sourceText, int line) {
        return new AtLine(sourceName, sourceText, line);
    }

    public static final DeclarationInTextSource atPosition(String sourceName, String sourceText, int line, int position) {
        return new AtPosition(sourceName, sourceText, line,position);
    }

    private static class InSource extends DeclarationInTextSource {

        public InSource(String sourceName, String sourceText) {
            super(sourceName, sourceText);
        }

        @Override
        public String toString() {
            return getSourceName();
        }

    }

    private static class AtLine extends DeclarationInTextSource {

        private final int line;

        public AtLine(String sourceName, String sourceText, int line) {
            super(sourceName, sourceText);
            this.line = line;
        }

        @Override
        public String toString() {
            return String.format("%s:%d", getSourceName(),line);
        }

    }

    private static class AtPosition extends DeclarationInTextSource {

        private int line;
        private int character;

        public AtPosition(String sourceName, String sourceText, int line, int character) {
            super(sourceName, sourceText);
            this.line = line;
            this.character = character;
        }

        @Override
        public String toString() {
            return String.format("%s:%d:%d", getSourceName(),line,character);
        }

    }

}
