package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class UInt16EffectiveStatementImpl extends UnsignedIntegerEffectiveImplBase {

    public static final String LOCAL_NAME = TypeUtils.UINT16;

    private static final Number MAX_RANGE = 65535;

    private static final String DESCRIPTION = LOCAL_NAME + " represents integer values between " + MIN_RANGE + " and "
            + MAX_RANGE + ", inclusively.";

    public UInt16EffectiveStatementImpl(final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

        super(ctx, LOCAL_NAME, MAX_RANGE, DESCRIPTION);
    }

    @Override
    public String toString() {
        return "type " + qName;
    }
}
