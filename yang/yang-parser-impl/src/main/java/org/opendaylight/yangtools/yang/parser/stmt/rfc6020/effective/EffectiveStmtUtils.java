package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class EffectiveStmtUtils {

    private EffectiveStmtUtils(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static final SourceException createNameCollisionSourceException(
            final StmtContext<?, ?, ?> ctx,
            EffectiveStatement<?, ?> effectiveStatement) {
        return new SourceException("Error in module '"
                + ctx.getRoot().getStatementArgument()
                + "': can not add '"
                + effectiveStatement.argument()
                + "'. Node name collision: '"
                + effectiveStatement.argument()
                + "' already declared.",
                ctx.getStatementSourceReference());
    }

}
