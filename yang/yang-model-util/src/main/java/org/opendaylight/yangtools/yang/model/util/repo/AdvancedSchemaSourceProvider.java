/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;

public interface AdvancedSchemaSourceProvider<F> extends SchemaSourceProvider<F> {

    /**
     * Returns source for supplied YANG source identifier.
     *
     * @param sourceIdentifier source identifier.
     * @return source representation if supplied YANG module is available
     *  {@link Optional#absent()} otherwise.
     */
    Optional<F> getSchemaSource(SourceIdentifier sourceIdentifier);
}
