package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

public interface StatementSourceReference {


    StatementSource getStatementSource();

    /**
     * Returns human readable representation of statement source.
     *
     * @return
     */
    @Override
    String toString();

}
