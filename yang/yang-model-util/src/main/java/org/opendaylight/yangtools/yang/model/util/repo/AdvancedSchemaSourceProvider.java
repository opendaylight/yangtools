/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;

/**
 * Provider of representation of YANG schema sources.
 *
 * {@link AdvancedSchemaSourceProvider} is replacement of
 * {@link SchemaSourceProvider} which did not have object representing source
 * identifier, and adds {@link SourceIdentifier} to identify
 * sources. but used name and revision disconnected
 *
 *
 * @param <T>
 *            Representation of Schema Source
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
