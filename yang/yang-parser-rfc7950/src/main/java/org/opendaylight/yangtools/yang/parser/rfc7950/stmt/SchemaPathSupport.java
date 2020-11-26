/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class SchemaPathSupport {

    private static final String SCHEMANODE_GETPATH_FORBID_PROPERTY = "schemanode.getpath.forbid";
    private static final boolean FORBID_SCHEMANODE_GETPATH =
            System.getProperty(SCHEMANODE_GETPATH_FORBID_PROPERTY, "disabled").equals("enabled");

    private SchemaPathSupport() {
    }

    public static SchemaPath getPath(final SchemaPath path) {
        // Forbid creating the SchemaPath in SchemaNode if property "schemanode.getpath.forbid" is set to "enabled"
        // schemanode.getpath.forbid=enabled
        if (FORBID_SCHEMANODE_GETPATH) {
            throw new UnsupportedOperationException(SchemaPathSupport.SCHEMANODE_GETPATH_FORBID_PROPERTY
                    + " property enabled - creating the SchemaPath is forbidden in SchemaNode");
        }
        return path;
    }
}
