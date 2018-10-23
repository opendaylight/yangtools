/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

public class YangSyntaxErrorException extends YangParserException {
    private static final long serialVersionUID = 2L;

    private final SourceIdentifier source;
    private final int line;
    private final int charPositionInLine;

    public YangSyntaxErrorException(final @Nullable SourceIdentifier source, final int line,
            final int charPositionInLine, final String message) {
        this(source, line, charPositionInLine, message, null);
    }

    public YangSyntaxErrorException(final @Nullable SourceIdentifier source, final int line,
            final int charPositionInLine, final String message, final @Nullable Throwable cause) {
        super(message, cause);
        this.source = source;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public final Optional<SourceIdentifier> getSource() {
        return Optional.ofNullable(source);
    }

    public final int getLine() {
        return line;
    }

    public final int getCharPositionInLine() {
        return charPositionInLine;
    }

    public @NonNull String getFormattedMessage() {
        final StringBuilder sb = new StringBuilder(getMessage());
        if (source != null) {
            sb.append(" in source ");
            sb.append(source);
        }
        if (line != 0) {
            sb.append(" on line ");
            sb.append(line);
            if (charPositionInLine != 0) {
                sb.append(" character ");
                sb.append(charPositionInLine);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + getFormattedMessage();
    }
}
