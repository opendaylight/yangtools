package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

public abstract class DeclarationInTextSource implements StatementSourceReference {

    @Override
    public StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

}
