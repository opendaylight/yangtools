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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
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
@NonNullByDefault
public sealed interface IdentifierBinding extends Immutable permits DefaultIdentifierBinding {
    /**
     * {@return an instance backed by a {@link NamespaceBinding}}
     * @param namespaceBinding the {@link NamespaceBinding}
     */
    static IdentifierBinding of(final NamespaceBinding namespaceBinding) {
        final var identifier = new IdentifierParser(namespaceBinding);
        final var nodeIdentifier = new NodeIdentifierParser(identifier);
        final var descendantSchemaNodeid = new DescendantSchemaNodeidParser(nodeIdentifier);
        final var absoluteSchemaNodeid = new AbsoluteSchemaNodeidParser(descendantSchemaNodeid);
        return new DefaultIdentifierBinding(descendantSchemaNodeid, absoluteSchemaNodeid, nodeIdentifier,
            namespaceBinding, identifier);
    }

    /**
     * {@return the backing {@link DescendantSchemaNodeidParser}}
     */
    DescendantSchemaNodeidParser descendantSchemaNodeid();

    /**
     * {@return the backing {@link AbsoluteSchemaNodeidParser}}
     */
    AbsoluteSchemaNodeidParser absoluteSchemaNodeid();

    /**
     * {@return the backing {@link NodeIdentifierParser}}
     */
    NodeIdentifierParser nodeIdentifier();

    /**
     * {@return the backing {@link NamespaceBinding}}
     */
    NamespaceBinding namespaceBinding();

    /**
     * {@return the backing {@link IdentifierParser}}
     */
    IdentifierParser identifier();

    /**
     * Parse a statement argument as an {@code identifier-arg}.
     *
     * @param stmt the statement
     * @param rawArgument the argument
     * @return a {@link QName}
     * @throws SourceException if {@code arg} is not a valid {@code identifier-arg}
     */
    QName parseIdentifierArg(CommonStmtCtx stmt, String rawArgument);

    /**
     * Parse a statement argument as an {@code identifier-arg}.
     *
     * @param qualifier the string used to qualify the argument name
     * @param stmt the statement
     * @param rawArgument the argument
     * @return a {@link QName}
     * @throws SourceException if {@code arg} is not a valid {@code identifier-arg}
     */
    QName parseIdentifierArg(String qualifier, CommonStmtCtx stmt, String rawArgument);

    /**
     * Parse a statement argument as an {@code identifier-ref-arg}.
     *
     * @param stmt the statement
     * @param rawArgument the argument
     * @return a {@link QName}
     * @throws SourceException if {@code arg} is not a valid {@code identifier-ref-arg}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    QName parseIdentifierRefArg(CommonStmtCtx stmt, String rawArgument);

    /**
     * Parse a statement argument as an {@code node-identifier-arg}.
     *
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return An interned QName
     * @throws SourceException if the string is not a valid YANG node identifier
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    QName parseNodeIdentifierArg(CommonStmtCtx stmt, String rawArgument);

    /**
     * Create a {@code node-identifier} from its {@code prefix} and {@code identifier}.
     *
     * @param stmt the statement
     * @param prefix the prefix
     * @param localName the identifier
     * @return An interned QName
     * @throws SourceException if the string is not a valid YANG node identifier
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    @Beta
    QName createNodeIdentifier(CommonStmtCtx stmt, String prefix, String localName);

    /**
     * Parse a statement argument as an {@code absolute-schema-nodeid}.
     *
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return An absolute schema node identifier
     * @throws SourceException if the string is not a valid {@code absolute-schema-nodeid}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    SchemaNodeIdentifier.Absolute parseAbsoluteSchemaNodeid(CommonStmtCtx stmt, String rawArgument);

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
    SchemaNodeIdentifier.Absolute parseAbsoluteSchemaNodeidAs(String production, CommonStmtCtx stmt,
            String rawArgument);

    /**
     * Parse a statement argument as an {@code descendant-schema-nodeid}.
     *
     * @param stmt the statement
     * @param rawArgument String to be parsed
     * @return A descendant schema node identifier
     * @throws SourceException if the string is not a valid {@code descendant-schema-nodeid}
     * @throws InferenceException if YANG node identifier's module cannot be resolved
     */
    SchemaNodeIdentifier.Descendant parseDescendantSchemaNodeid(CommonStmtCtx stmt, String rawArgument);

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
    SchemaNodeIdentifier.Descendant parseDescendantSchemaNodeidAs(String production, CommonStmtCtx stmt,
        String rawArgument);
}
