package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface AugmentStatement extends DeclaredStatement<SchemaNodeIdentifier> , DataDefinitionContainer {

    String getTargetNode();

}
