/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import java.io.InputStream;

import com.google.common.base.Optional;
import com.google.common.io.ByteSource;

/**
 * Provider of representation of YANG schema sources.
 *
 * <p>
 * {@link AdvancedSchemaSourceProvider} is extension of
 * {@link SchemaSourceProvider} which did not have object concept of source
 * identifier, and introduces {@link SourceIdentifier}
 * (which contains schema name and revision) as identifier of sources.
 *
 * <p>
 * <b>Representation of Schema Source</b> Schema source may be represented by
 * various Java Types, which depends on provider and may be consumer, same of
 * possible representations are:
 * <ul>
 * <li>{@link String}
 * <li>{@link InputStream}
 * <li>{@link ByteSource}
 * <li>Parsed AST
 * </ul>
 *
 * <p>
 * Conversion between representations should be done via implementations of
 * {@link SchemaSourceTransformation}.
 *
 * @param <T>
 *            Representation of Schema source provided by this implementation
 */
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
