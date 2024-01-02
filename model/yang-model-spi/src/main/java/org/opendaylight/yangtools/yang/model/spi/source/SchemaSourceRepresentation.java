/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Common interface for schema source representations. A schema source is an atomic piece of the overall schema context.
 * In YANG terms, a schema source is semantically equivalent to a single YANG text file, be it a module or a submodule.
 *
 * <p>
 * A schema source can exist in various forms, which we call representations. Again, in YANG terms, each representation
 * is semantically equivalent, but from implementation perspective certain operations on a schema source may require it
 * to be first transformed into a particular representation before they can be applied. Such transformations are
 * affected via instances of SchemaSourceTransformation.
 *
 * <p>
 * Typical examples of a schema source representation include:
 * <ul>
 * <li>a {@link java.lang.String} - textual representation of source code
 * <li>a {@link java.io.InputStream} - input stream containing source code
 * <li>a {@link com.google.common.io.ByteSource} - source for input streams
 * containing source code
 * <li>Parsed abstract syntax tree (AST), which is the result of a syntactic parser
 * </ul>
 *
 * <p>
 * Implementations of this interface expected to comply with the {@link Immutable} contract.
 */
public sealed interface SchemaSourceRepresentation extends Immutable
        permits YangSchemaSourceRepresentation, YinSchemaSourceRepresentation {
    /**
     * The {@link SourceIdentifier} of this source.
     *
     * @return {@link SourceIdentifier} of this source
     */
    @NonNull SourceIdentifier sourceId();

    /**
     * Return the concrete representation type.
     *
     * @return The type of representation.
     */
    @NonNull Class<? extends SchemaSourceRepresentation> getType();

    /**
     * Return the symbolic name, if available. This name has no semantic meaning beyond being useful for debugging
     * by humans.
     *
     * @return Symbolic name, if available
     */
    Optional<String> getSymbolicName();
}
