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
 * Implementation of Yang int8 built-in type. <br>
 * int8 represents integer values between -128 and 127, inclusively. The Java
 * counterpart of Yang int8 built-in type is {@link Byte}.
 *
 * @see AbstractSignedInteger
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#int8Type()} instead
 */
@Deprecated
public final class Int8 extends AbstractSignedInteger implements Immutable {
    private static final String DESCRIPTION = "represents integer values between -128 and 127, inclusively.";

    private Int8() {
        super(BaseTypes.INT8_QNAME, DESCRIPTION, Byte.MIN_VALUE, Byte.MAX_VALUE, "");
    }

    private static final Int8 INSTANCE = new Int8();

    /**
     * Returns default instance of int8 type.
     * @return default instance of int8 type.
     */
    public static Int8 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + BaseTypes.INT8_QNAME;
    }

}
