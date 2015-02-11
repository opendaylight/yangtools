package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ImportStatementImpl extends AbstractDeclaredStatement<String> implements ImportStatement {

    ImportStatementImpl(StmtContext<String, ImportStatement,?> context) {
        super(context);
    }

    @Override
    public String getModule() {
        return rawArgument();
    }

    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

    @Override
    public RevisionDateStatement getRevisionDate() {
        return firstDeclared(RevisionDateStatement.class);
    }
}
