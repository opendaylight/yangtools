package org.opendaylight.yangtools.yang.parser.rfc7950;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaNodePropertyUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaNodePropertyUtil.class);
    public static final String SCHEMANODE_GETPATH_FORBID_PROPERTY = "schemanode.getpath.forbid";

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
