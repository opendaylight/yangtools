/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class Int32EffectiveStatementImpl extends IntegerEffectiveImplBase {

    public static final String LOCAL_NAME = TypeUtils.INT32;

    private static final Number MIN_RANGE = Integer.MIN_VALUE;
    private static final Number MAX_RANGE = Integer.MAX_VALUE;

    private static final String DESCRIPTION = LOCAL_NAME + " represents integer values between " + MIN_RANGE + " and "
            + MAX_RANGE + ", inclusively.";

    public Int32EffectiveStatementImpl(final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

        super(ctx, BaseTypes.INT32_QNAME, MIN_RANGE, MAX_RANGE, DESCRIPTION);
    }

    @Override
    public String toString() {
        return "type " + getQName();
    }
}
