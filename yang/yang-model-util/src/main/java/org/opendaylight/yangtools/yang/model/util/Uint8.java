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
 * Implementation of Yang uint8 built-in type. <br>
 * uint8 represents integer values between 0 and 255, inclusively.
 *
 * @see AbstractUnsignedInteger
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#uint8Type()} instead
 */
@Deprecated
public final class Uint8 extends AbstractUnsignedInteger implements Immutable {
    public static final int MAX_VALUE = 255;
    private static final String DESCRIPTION = "uint8  represents integer values between 0 and 255, inclusively.";

    private static final Uint8 INSTANCE = new Uint8();

    private Uint8() {
        super(BaseTypes.UINT8_QNAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint8 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + BaseTypes.UINT8_QNAME;
    }

}
