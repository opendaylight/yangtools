/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Registry of all potentially available schema sources. Processes capable of dynamic schema discovery, such as OSGi
 * registry scanners, NETCONF clients (with NETCONF monitoring extension) and similar can register
 * {@link SchemaSourceProvider} instances which would then acquire the schema source.
 */
@Beta
public interface SchemaSourceRegistry {
    /**
     * Register a new schema source which is potentially available from a provider. A registration does not guarantee
     * that a subsequent call to {@link SchemaSourceProvider#getSource(SourceIdentifier)} will succeed.
     *
     * @param <T> schema source representation type
     * @param provider Resolver which can potentially resolve the identifier
     * @param source Schema source details
     * @return A registration handle. Invoking {@link SchemaSourceRegistration#close()} will cancel the registration.
     */
    <T extends SchemaSourceRepresentation> SchemaSourceRegistration<T> registerSchemaSource(
            SchemaSourceProvider<? super T> provider, PotentialSchemaSource<T> source);

    /**
     * Register a schema source listener. The listener will be notified as new sources and their representations become
     * available, subject to the provided filter.
     *
     * @param listener Schema source listener
     * @return A registration handle. Invoking {@link Registration#close()} will cancel the registration.
     */
    Registration registerSchemaSourceListener(SchemaSourceListener listener);
}
