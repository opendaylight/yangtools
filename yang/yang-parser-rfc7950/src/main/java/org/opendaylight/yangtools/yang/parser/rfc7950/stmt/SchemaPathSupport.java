/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchemaPathSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaPathSupport.class);
    public static final String SCHEMANODE_GETPATH_FORBID_PROPERTY = "schemanode.getpath.forbid";

    private SchemaPathSupport() {
    }

    public static SchemaPath getPath(final SchemaPath path) {
        // Forbid creating the SchemaPath in SchemaNode
        // defaults: schemanode.getpath.forbid=disabled
        final String propValue = System.getProperty(SCHEMANODE_GETPATH_FORBID_PROPERTY, "disabled");
        switch (propValue) {
            case "enabled":
                throw new UnsupportedOperationException(SchemaPathSupport.SCHEMANODE_GETPATH_FORBID_PROPERTY
                        + " property enabled - creating the SchemaPath is forbidden in SchemaNode");
            case "disabled":
                break;
            default:
                LOG.warn("Unhandled {} value \"{}\", assuming disabled", SCHEMANODE_GETPATH_FORBID_PROPERTY, propValue);
        }

        return path;
    }
}
