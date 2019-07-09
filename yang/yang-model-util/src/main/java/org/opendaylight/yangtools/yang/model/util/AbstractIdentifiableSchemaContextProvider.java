/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@NonNullByDefault
public abstract class AbstractIdentifiableSchemaContextProvider<T> extends AbstractSchemaContextProvider
        implements Identifiable<T> {
    private final @NonNull T identifier;

    protected AbstractIdentifiableSchemaContextProvider(final SchemaContext schemaContext, final T identifier) {
        super(schemaContext);
        this.identifier = requireNonNull(identifier);
    }

    @Override
    public final T getIdentifier() {
        return identifier;
    }

}
