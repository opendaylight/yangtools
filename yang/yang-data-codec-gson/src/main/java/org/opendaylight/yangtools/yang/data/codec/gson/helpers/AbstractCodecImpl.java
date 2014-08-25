/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.helpers;

import com.google.common.base.Preconditions;

import java.net.URI;

import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCodecImpl {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCodecImpl.class);
    private final SchemaContextUtils schema;

    protected AbstractCodecImpl(final SchemaContextUtils schema) {
        this.schema = Preconditions.checkNotNull(schema);
    }

    protected final SchemaContextUtils getSchema() {
        return schema;
    }

    protected final Module getModuleByNamespace(final String namespace) {
        URI validNamespace = resolveValidNamespace(namespace);

        Module module = schema.findModuleByNamespace(validNamespace);
        if (module == null) {
            LOG.info("Module for namespace " + validNamespace + " wasn't found.");
            return null;
        }
        return module;
    }

    protected final URI resolveValidNamespace(final String namespace) {
        URI validNamespace = schema.findNamespaceByModuleName(namespace);
        if (validNamespace == null) {
            validNamespace = URI.create(namespace);
        }

        return validNamespace;
    }
}
