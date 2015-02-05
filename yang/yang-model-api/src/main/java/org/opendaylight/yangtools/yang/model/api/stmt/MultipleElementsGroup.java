package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;

public interface MultipleElementsGroup {

    @Nullable MinElementsStatement getMinElements();

    @Nullable MaxElementsStatement getMaxElements();

    @Nullable OrderedByStatement getOrderedBy();
}
