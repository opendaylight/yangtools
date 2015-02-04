package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TypedGroup {


    @Nonnull TypeStatement getType();

    @Nullable UnitsStatement getUnits();
}
