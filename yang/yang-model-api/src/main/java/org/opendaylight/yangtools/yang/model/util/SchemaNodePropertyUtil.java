/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SchemaNodePropertyUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaNodePropertyUtil.class);
    public static final String SCHEMANODE_GETPATH_FORBID_PROPERTY = "schemanode.getpath.forbid";

    private SchemaNodePropertyUtil() {
    }

    public static boolean isGetPathForbidden() {
        // Forbid creating SchemaPath in SchemaNode
        // forbid SchemaNode.getPath
        // schemanode.getpath.forbid=enabled
        final String propValue = System.getProperty(SCHEMANODE_GETPATH_FORBID_PROPERTY, "disabled");
        switch (propValue) {
            case "enabled":
                return true;
            case "disabled":
                break;
            default:
                LOG.warn("Unhandled {} value \"{}\", assuming disabled", SCHEMANODE_GETPATH_FORBID_PROPERTY, propValue);
        }

        return false;
    }
}
