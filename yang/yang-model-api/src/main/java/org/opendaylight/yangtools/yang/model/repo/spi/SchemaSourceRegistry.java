/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Registry of all potentially available schema sources. Processes capable of
 * dynamic schema discovery, such as OSGi registry scanners, NETCONF clients
 * (with NETCONF monitoring extension) and similar can register
 * {@link SchemaSourceProvider} instances which would then acquire the schema
 * source.
 */
public interface SchemaSourceRegistry {
    /**
     * Register a new schema source which is potentially available from a provider.
     * A registration does not guarantee that a subsequent call to
     * {@link SchemaSourceProvider#getSource(SourceIdentifier)} will succeed.
     *
     * @param identifier Schema source identifier
     * @param provider Resolver which can potentially resolve the identifier
     * @param representation Schema source representation which the source may
     *                       be available.
     * @return A registration handle. Invoking {@link SchemaSourceRegistration#close()}
     *         will cancel the registration.
     */
    <T extends SchemaSourceRepresentation> SchemaSourceRegistration registerSchemaSource(
            SourceIdentifier identifier, SchemaSourceProvider<? super T> provider, Class<T> representation);

    /**
     * Register a schema transformer. The registry can invoke it to transform between
     * the various schema source formats.
     *
     * @param transformer Schema source transformer
     * @return A registration handle. Invoking {@link SchemaTransformerRegistration#close()}
     *         will cancel the registration.
     */
    SchemaTransformerRegistration registerSchemaSourceTransformer(SchemaSourceTransformer<?, ?> transformer);
}
