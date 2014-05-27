/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Implementation of Yang int32 built-in type. <br>
 * int32 represents integer values between -2147483648 and 2147483647,
 * inclusively. The Java counterpart of Yang int32 built-in type is
 * {@link Integer}.
 *
 * @see AbstractSignedInteger
 *
 */
public final class Int32 extends AbstractSignedInteger implements Immutable {
    private static final Int32 INSTANCE = new Int32();
    private static final QName NAME = BaseTypes.constructQName("int32");
    private static final String DESCRIPTION = "int32  represents integer values between -2147483648 and 2147483647, inclusively.";

    private Int32() {
        super(Int32.NAME, Int32.DESCRIPTION, Integer.MIN_VALUE, Integer.MAX_VALUE, "");
    }

    public static Int32 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + NAME;
    }
}
