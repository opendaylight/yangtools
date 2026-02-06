/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
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

@NonNullByDefault
record DefaultIdentifierBinding(
        DescendantSchemaNodeidParser descendantSchemaNodeid,
        AbsoluteSchemaNodeidParser absoluteSchemaNodeid,
        NodeIdentifierParser nodeIdentifier,
        NamespaceBinding namespaceBinding,
        IdentifierParser identifier) implements IdentifierBinding {
    DefaultIdentifierBinding {
        requireNonNull(descendantSchemaNodeid);
        requireNonNull(absoluteSchemaNodeid);
        requireNonNull(nodeIdentifier);
        requireNonNull(namespaceBinding);
        requireNonNull(identifier);
    }

    @Override
    public QName parseIdentifierArg(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return identifier.parseArgument(rawArgument);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    @Override
    public QName parseIdentifierArg(final String qualifier, final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return identifier.parseArgument(rawArgument);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(qualifier, stmt, rawArgument, e);
        }
    }

    @Override
    public QName parseIdentifierRefArg(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return nodeIdentifier.parseNodeIdentifierAs("identifier-ref-arg", rawArgument, 0, rawArgument.length());
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    @Override
    public QName parseNodeIdentifierArg(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return nodeIdentifier.parseArgument(rawArgument);
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    @Override
    public QName createNodeIdentifier(final CommonStmtCtx stmt, final String prefix, final String localName) {
        try {
            return nodeIdentifier.createNodeIdentifier(prefix, localName);
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, prefix + ":" + identifier, e);
        }
    }

    @Override
    public SchemaNodeIdentifier.Absolute parseAbsoluteSchemaNodeid(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return absoluteSchemaNodeid.parseArgument(rawArgument);
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    @Override
    public SchemaNodeIdentifier.Absolute parseAbsoluteSchemaNodeidAs(final String production, final CommonStmtCtx stmt,
            final String rawArgument) {
        try {
            return absoluteSchemaNodeid.parseAbsoluteSchemaNodeidAs(production, rawArgument, 0, rawArgument.length());
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    @Override
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

    @Override
    public SchemaNodeIdentifier.Descendant parseDescendantSchemaNodeidAs(final String production,
            final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return descendantSchemaNodeid.parseDescendantSchemaNodeidAs(production, rawArgument, 0,
                rawArgument.length());
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(IdentifierBinding.class).add("namespaceBinding", namespaceBinding).toString();
    }

    private static SourceException newSourceException(final CommonStmtCtx stmt,
            final String rawArgument, final ArgumentSyntaxException cause) {
        // qualify with statement's name
        return newSourceException(stmt.publicDefinition().statementName().getLocalName(), stmt, rawArgument, cause);
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
            .append(stmt.publicDefinition().getArgumentDefinition().argumentName().getLocalName());
        final var position = cause.getPosition();
        if (position != 0) {
            sb.append(" on position ").append(position);
        }
        return new SourceException(sb.append(": ").append(cause.getMessage()).toString(), stmt, cause);
    }
}
