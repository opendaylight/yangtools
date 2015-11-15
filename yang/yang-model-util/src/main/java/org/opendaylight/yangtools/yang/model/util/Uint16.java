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
 * Implementation of Yang uint16 built-in type. <br>
 * uint16 represents integer values between 0 and 65535, inclusively. The Java
 * counterpart of Yang uint16 built-in type is {@link Integer}.
 *
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#uint16Type()} instead
 */
@Deprecated
public final class Uint16 extends AbstractUnsignedInteger implements Immutable {
    public static final int MAX_VALUE = 65535;
    private static final String DESCRIPTION = "uint16 represents integer values between 0 and 65535, inclusively.";

    private static final Uint16 INSTANCE = new Uint16();

    private Uint16() {
        super(BaseTypes.UINT16_QNAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint16 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + BaseTypes.UINT16_QNAME;
    }

}
