/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.stmt.AbsoluteSchemaNodeidParser;
import org.opendaylight.yangtools.yang.model.spi.stmt.DescendantSchemaNodeidParser;
import org.opendaylight.yangtools.yang.model.spi.stmt.IdentifierParser;
import org.opendaylight.yangtools.yang.model.spi.stmt.NamespaceBinding;
import org.opendaylight.yangtools.yang.model.spi.stmt.NodeIdentifierParser;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * A collection of argument parsers related to {@code identifier} and its various manifestations.
 *
 * @since 14.0.22
 */
@Beta
@NonNullByDefault
public final class IdentifierBinding {
    public AbsoluteSchemaNodeidParser absoluteSchemaNodeid;
    public DescendantSchemaNodeidParser descendantSchemaNodeid;
    public NodeIdentifierParser nodeIdentifier;
    public IdentifierParser identifier;

    /**
     * Construct an instance backed by a {@link NamespaceBinding}.
     *
     * @param namespaceBinding the {@link NamespaceBinding}
     */
    public IdentifierBinding(final NamespaceBinding namespaceBinding) {
        identifier = new IdentifierParser(namespaceBinding);
        nodeIdentifier = new NodeIdentifierParser(identifier);
        descendantSchemaNodeid = new DescendantSchemaNodeidParser(nodeIdentifier);
        absoluteSchemaNodeid = new AbsoluteSchemaNodeidParser(descendantSchemaNodeid);
    }

    /**
     * Parse a statement argument as an {@code identifier-arg}.
     *
     * @param stmt the statement
     * @param rawArgument the argument
     * @return a {@link QName}
     * @throws SourceException if {@code arg} is not a valid {@code identifier-arg}
     */
    public QName parseIdentifierArg(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return identifier.parseArgument(rawArgument);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    /**
     * Parse a statement argument as an {@code identifier-arg}.
     *
     * @param qualifier the string used to qualify the argument name
     * @param stmt the statement
     * @param rawArgument the argument
     * @return a {@link QName}
     * @throws SourceException if {@code arg} is not a valid {@code identifier-arg}
     */
    public QName parseIdentifierArg(final String qualifier, final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return identifier.parseArgument(rawArgument);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(qualifier, stmt, rawArgument, e);
        }
    }

    /**
     * Parse a statement argument as an {@code node-identifier-arg}.
     *
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return An interned QName
     * @throws SourceException if the string is not a valid YANG node identifier
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    public QName parseNodeIdentifierArg(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return nodeIdentifier.parseArgument(rawArgument);
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    /**
     * Parse a statement argument as an {@code absolute-schema-nodeid}.
     *
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return An absolute schema node identifier
     * @throws SourceException if the string is not a valid {@code absolute-schema-nodeid}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    public SchemaNodeIdentifier.Absolute parseAbsoluteSchemaNodeid(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return absoluteSchemaNodeid.parseArgument(rawArgument);
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    /**
     * Parse a statement argument as an {@code descendant-schema-nodeid}.
     *
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return A descendant schema node identifier
     * @throws SourceException if the string is not a valid {@code descendant-schema-nodeid}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    public SchemaNodeIdentifier.Descendant parseDescendantSchemaNodeid(final CommonStmtCtx stmt,
            final String rawArgument) {
        try {
            return descendantSchemaNodeid.parseArgument(rawArgument);
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    private static SourceException newSourceException(final CommonStmtCtx stmt,
            final String rawArgument, final ArgumentSyntaxException cause) {
        // qualify with statement's name
        return newSourceException(stmt.publicDefinition().getStatementName().getLocalName(), stmt, rawArgument, cause);
    }

    /**
     * Return a new {@link SourceException} that is reporting an {@link ArgumentSyntaxException} while parsing a
     * statement argument.
     *
     * @param qualifier the string used to qualify the argument name
     * @param stmt Statement context, not retained
     * @param rawArgument the argument value being parsed
     * @param cause the {@link ArgumentSyntaxException} cause
     * @return a new {@link SourceException}
     */
    private static SourceException newSourceException(final String qualifier, final CommonStmtCtx stmt,
            final String rawArgument, final ArgumentSyntaxException cause) {
        final var sb = new StringBuilder()
            .append('\'').append(rawArgument).append("' is not a valid ").append(qualifier).append(' ')
            .append(stmt.publicDefinition().getArgumentDefinition().orElseThrow().argumentName().getLocalName());
        final var position = cause.getPosition();
        if (position != 0) {
            sb.append(" on position ").append(position);
        }
        return new SourceException(sb.append(": ").append(cause.getMessage()).toString(), stmt, cause);
    }
}
