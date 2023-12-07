/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

/**
 * Listener on {@link SchemaSourceRepresentation} lifecycle
 */
@Beta
@NonNullByDefault
public interface SchemaSourceListener {
    /**
     * Invoked when the registry sees a concrete source. This callback is typically used by cache-type listeners, who
     * intercept the source, store it locally and announce themselves as a provider of that particular schema source.
     *
     * @param source Schema source
     */
    void schemaSourceEncountered(SchemaSourceRepresentation source);

    /**
     * Invoked when a new schema source is registered by a provider. This call
     * callback, along with {@link #schemaSourceUnregistered(PotentialSchemaSource)}
     * is typically used by transformer-type listeners, who intercept the registration
     * if the advertised representation matches their input type and register
     * themselves as a potential provider of the same source in their output
     * representation type.
     *
     * @param sources Newly available sources
     */
    void schemaSourceRegistered(Iterable<PotentialSchemaSource<?>> sources);

    /**
     * Invoked when a schema source is unregistered.
     *
     * @param source Schema source representation
     */
    void schemaSourceUnregistered(PotentialSchemaSource<?> source);
}
