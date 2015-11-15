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
 * Implementation of Yang uint32 built-in type. <br>
 * uint32 represents integer values between 0 and 4294967295, inclusively.
 *
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#uint32Type()} instead
 */
@Deprecated
public final class Uint32 extends AbstractUnsignedInteger implements Immutable {
    public static final long MAX_VALUE = 4294967295L;
    private static final String DESCRIPTION = "uint32 represents integer values between 0 and 4294967295, inclusively.";

    private static final Uint32 INSTANCE = new Uint32();


    private Uint32() {
        super(BaseTypes.UINT32_QNAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint32 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + BaseTypes.UINT32_QNAME;
    }

}
