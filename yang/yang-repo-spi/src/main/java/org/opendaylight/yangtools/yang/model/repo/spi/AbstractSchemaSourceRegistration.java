/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

public abstract class AbstractSchemaSourceRegistration<T extends SchemaSourceRepresentation>
        extends AbstractObjectRegistration<PotentialSchemaSource<T>> {
    private final SchemaSourceProvider<?> provider;

    protected AbstractSchemaSourceRegistration(final SchemaSourceProvider<?> provider,
            final PotentialSchemaSource<T> source) {
        super(source);
        this.provider = requireNonNull(provider);
    }

    public final SchemaSourceProvider<?> getProvider() {
        return provider;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("provider", provider);
    }
}
