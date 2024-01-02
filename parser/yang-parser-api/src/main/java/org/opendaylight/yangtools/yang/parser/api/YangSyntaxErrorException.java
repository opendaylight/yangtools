/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

@Beta
public class YangSyntaxErrorException extends YangParserException {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final SourceIdentifier sourceId;
    private final int line;
    private final int charPositionInLine;

    public YangSyntaxErrorException(final @Nullable SourceIdentifier sourceId, final int line,
            final int charPositionInLine, final String message) {
        this(sourceId, line, charPositionInLine, message, null);
    }

    public YangSyntaxErrorException(final @Nullable SourceIdentifier sourceId, final int line,
            final int charPositionInLine, final String message, final @Nullable Throwable cause) {
        super(message, cause);
        this.sourceId = sourceId;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public final Optional<SourceIdentifier> getSource() {
        return Optional.ofNullable(sourceId);
    }

    public final int getLine() {
        return line;
    }

    public final int getCharPositionInLine() {
        return charPositionInLine;
    }

    public @NonNull String getFormattedMessage() {
        final StringBuilder sb = new StringBuilder(getMessage());
        if (sourceId != null) {
            sb.append(" in source ").append(sourceId);
        }
        if (line != 0) {
            sb.append(" on line ").append(line);
            if (charPositionInLine != 0) {
                sb.append(" character ").append(charPositionInLine);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + getFormattedMessage();
    }
}
