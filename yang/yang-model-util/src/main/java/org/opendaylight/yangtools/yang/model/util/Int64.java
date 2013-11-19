/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Implementation of Yang int64 built-in type. <br>
 * int64 represents integer values between -9223372036854775808 and
 * 9223372036854775807, inclusively. The Java counterpart of Yang int64 built-in
 * type is {@link Long}.
 *
 */
public final class Int64 extends AbstractSignedInteger {
    private static Int64 instance;
    private static final QName NAME = BaseTypes.constructQName("int64");
    private static final String DESCRIPTION = "int64  represents integer values between -9223372036854775808 and 9223372036854775807, inclusively.";

    private Int64() {
        super(NAME, DESCRIPTION, Long.MIN_VALUE, Long.MAX_VALUE, "");
    }

    public static Int64 getInstance() {
        if (instance == null) {
            instance = new Int64();
        }
        return instance;
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
