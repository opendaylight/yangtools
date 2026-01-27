/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
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
public final class IdentifierBinding implements NamespaceBinding {
    public final AbsoluteSchemaNodeidParser absoluteSchemaNodeid;
    public final DescendantSchemaNodeidParser descendantSchemaNodeid;
    public final NodeIdentifierParser nodeIdentifier;
    public final IdentifierParser identifier;
    public final NamespaceBinding delegate;

    /**
     * Construct an instance backed by a {@link NamespaceBinding}.
     *
     * @param delegate the {@link NamespaceBinding}
     */
    public IdentifierBinding(final NamespaceBinding delegate) {
        this.delegate = requireNonNull(delegate);
        identifier = new IdentifierParser(delegate);
        nodeIdentifier = new NodeIdentifierParser(identifier);
        descendantSchemaNodeid = new DescendantSchemaNodeidParser(nodeIdentifier);
        absoluteSchemaNodeid = new AbsoluteSchemaNodeidParser(descendantSchemaNodeid);
    }

    @Override
    public QNameModule currentModule() {
        return delegate.currentModule();
    }

    @Override
    public @Nullable QNameModule lookupModule(final Unqualified prefix) {
        return delegate.lookupModule(prefix);
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
     * Parse a statement argument as an {@code identifier-ref-arg}.
     *
     * @param stmt the statement
     * @param rawArgument the argument
     * @return a {@link QName}
     * @throws SourceException if {@code arg} is not a valid {@code identifier-ref-arg}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    public QName parseIdentifierRefArg(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return nodeIdentifier.parseNodeIdentifierAs("identifier-ref-arg", rawArgument, 0, rawArgument.length());
        } catch (ArgumentBindingException e) {
            throw new InferenceException(e.getMessage(), stmt, e);
        } catch (ArgumentSyntaxException e) {
            throw newSourceException(stmt, rawArgument, e);
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
     * Parse a statement argument as an {@code absolute-schema-nodeid} equivalent production.
     *
     * @param production the production being parsed
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return A descendant schema node identifier
     * @throws SourceException if the string is not a valid {@code absolute-schema-nodeid}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
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

    /**
     * Parse a statement argument as an {@code descendant-schema-nodeid} equivalent production.
     *
     * @param production the production being parsed
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return A descendant schema node identifier
     * @throws SourceException if the string is not a valid {@code descendant-schema-nodeid}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
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
        return new SourceException(
            formatMessage(qualifier, stmt.publicDefinition().getArgumentDefinition(), rawArgument, cause), stmt, cause);
    }

    static String formatMessage(final String qualifier, final ArgumentDefinition<?> def, final String rawArgument,
            final ArgumentSyntaxException cause) {
        final var sb = new StringBuilder()
            .append('\'').append(rawArgument).append("' is not a valid ").append(qualifier).append(' ')
            .append(def.argumentName().getLocalName());
        final var position = cause.getPosition();
        if (position != 0) {
            sb.append(" on position ").append(position);
        }
        return sb.append(": ").append(cause.getMessage()).toString();
    }
}
