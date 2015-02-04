package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.base.Supplier;

public interface StatementSupplier<S extends Statement<S>> extends Supplier<S> {

    /**
    *
    * Returns statement source.
    *
    * @return statement source.
    */
    StatementSource statementSource();

    @Override
    S get();

}
