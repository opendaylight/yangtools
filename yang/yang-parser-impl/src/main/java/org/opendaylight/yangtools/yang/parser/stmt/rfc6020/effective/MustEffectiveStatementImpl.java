package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class MustEffectiveStatementImpl extends
        EffectiveStatementBase<RevisionAwareXPath, MustStatement> {

    public MustEffectiveStatementImpl(
            StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        super(ctx);

    }

}