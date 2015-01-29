package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface DeviationStatement extends DeclaredStatement<SchemaNodeIdentifier> {


    @Nonnull String getTargetNode();

}
