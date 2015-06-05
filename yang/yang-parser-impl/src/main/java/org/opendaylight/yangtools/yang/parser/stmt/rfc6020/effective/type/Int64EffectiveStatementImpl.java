package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class Int64EffectiveStatementImpl extends IntegerEffectiveImplBase {

    public static final String LOCAL_NAME = TypeUtils.INT64;

    private static final Number MIN_RANGE = Long.MIN_VALUE;
    private static final Number MAX_RANGE = Long.MAX_VALUE;

    private static final String DESCRIPTION = LOCAL_NAME + " represents integer values between " + MIN_RANGE + " and "
            + MAX_RANGE + ", inclusively.";

    public Int64EffectiveStatementImpl(final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

        super(ctx, LOCAL_NAME, MIN_RANGE, MAX_RANGE, DESCRIPTION);
    }

    @Override
    public String toString() {
        return "type " + qName;
    }
}
