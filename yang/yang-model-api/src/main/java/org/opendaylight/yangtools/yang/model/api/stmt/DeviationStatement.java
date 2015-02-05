package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.Statement;

public interface DeviationStatement extends Statement<DeviationStatement> {


    @Nonnull String getTargetNode();

}
