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
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * Utility superclass for classes returning a constant {@link SchemaContext}.
 */
@Beta
@NonNullByDefault
public abstract class AbstractSchemaContextProvider implements SchemaContextProvider {
    private final SchemaContext schemaContext;

    protected AbstractSchemaContextProvider(final SchemaContext schemaContext) {
        this.schemaContext = requireNonNull(schemaContext);
    }

    @Override
    public final SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("schemaContext", schemaContext);
    }
}
