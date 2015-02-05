package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.meta.Statement;

public interface AugmentStatement extends Statement<AugmentStatement> {

    String getTargetNode();

}
