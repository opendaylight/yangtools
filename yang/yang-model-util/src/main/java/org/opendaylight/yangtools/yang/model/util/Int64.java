/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Implementation of Yang int64 built-in type. <br>
 * int64 represents integer values between -9223372036854775808 and
 * 9223372036854775807, inclusively. The Java counterpart of Yang int64 built-in
 * type is {@link Long}.
 *
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#int64Type()} instead
 */
@Deprecated
public final class Int64 extends AbstractSignedInteger implements Immutable {
    private static final String DESCRIPTION = "int64  represents integer values between -9223372036854775808 and 9223372036854775807, inclusively.";

    private Int64() {
        super(BaseTypes.INT64_QNAME, DESCRIPTION, Long.MIN_VALUE, Long.MAX_VALUE, "");
    }


    private static final Int64 INSTANCE = new Int64();

    /**
     * Returns default instance of int64 type.
     * @return default instance of int64 type.
     */
    public static Int64 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + BaseTypes.INT64_QNAME;
    }
}
