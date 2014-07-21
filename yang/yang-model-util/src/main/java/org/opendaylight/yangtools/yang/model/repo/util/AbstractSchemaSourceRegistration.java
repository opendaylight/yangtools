/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.base.Preconditions;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;

public abstract class AbstractSchemaSourceRegistration extends AbstractObjectRegistration<SourceIdentifier> implements SchemaSourceRegistration {
    private final SchemaSourceProvider<?> provider;

    protected AbstractSchemaSourceRegistration(final SourceIdentifier identifier, final SchemaSourceProvider<?> provider) {
        super(identifier);
        this.provider = Preconditions.checkNotNull(provider);
    }

    protected SchemaSourceProvider<?> getProvider() {
        return provider;
    }
}
