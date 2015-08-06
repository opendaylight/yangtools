/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;

/**
 * Provider of representation of YANG schema sources.
 *
 * <p>
 * {@link AdvancedSchemaSourceProvider} is extension of
 * {@link SchemaSourceProvider} which did not have object concept of source
 * identifier, and introduces {@link SourceIdentifier} (which contains schema
 * name and revision) as identifier of sources.
 *
 * <p>
 * <b>Schema Source representation</b>
 * <p>
 * Representation of schema source. Representation of schema source could exists
 * in various formats (Java types), depending on stage of processing, but
 * representation MUST BE still result of processing of only single unit of schema
 * source (file, input stream). E.g.:
 * <ul>
 * <li>{@link java.lang.String} - textual representation of source code
 * <li>{@link java.io.InputStream} - input stream containing source code
 * <li>{@link com.google.common.io.ByteSource} - source for input streams
 * containing source code
 * <li>Parsed AST - abstract syntax tree, which is result of a parser, but still
 * it is not linked against other schemas.
 * </ul>
 *
 * <p>
 * Conversion between representations should be done via implementations of
 * {@link SchemaSourceTransformation}.
 *
 * @param <T>
 *            Schema source representation type provided by this implementation
 *
 * @deprecated Replaced with {@link org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider}
 * and related APIs.
 */
@Deprecated
public interface AdvancedSchemaSourceProvider<T> extends SchemaSourceProvider<T> {

    /**
     * Returns representation source for supplied YANG source identifier.
     *
     * Returned representation of schema source must be immutable, must not
     * change during runtime if {@link SourceIdentifier} has specified both
     * {@link SourceIdentifier#getName()} and
     * {@link SourceIdentifier#getRevision()}
     *
     * @param sourceIdentifier
     *            source identifier.
     * @return source representation if supplied YANG module is available
     *         {@link Optional#absent()} otherwise.
     */
    Optional<T> getSchemaSource(SourceIdentifier sourceIdentifier);
}
