/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.immutable;

import java.util.Date;
import java.util.Optional;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Value.Immutable
abstract class AbstractSchemaContext extends CommonContainerSchemaNode implements SchemaContext {

    @Override
    public final Optional<String> getModuleSource(final ModuleIdentifier moduleIdentifier) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Module findModuleByName(final String name, final Date revision) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
