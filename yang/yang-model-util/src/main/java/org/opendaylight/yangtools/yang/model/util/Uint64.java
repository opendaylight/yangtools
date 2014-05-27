/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.math.BigInteger;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Implementation of Yang uint64 built-in type. <br>
 * uint64 represents integer values between 0 and 18446744073709551615,
 * inclusively. The Java counterpart of Yang uint64 built-in type is
 * {@link BigInteger}.
 *
 */
public final class Uint64 extends AbstractUnsignedInteger implements Immutable {
    private static final Uint64 INSTANCE = new Uint64();
    public static final BigInteger MAX_VALUE = new BigInteger("18446744073709551615");
    private static final QName NAME = BaseTypes.constructQName("uint64");
    private static final String DESCRIPTION = "uint64 represents integer values between 0 and 18446744073709551615, inclusively.";

    private Uint64() {
        super(NAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint64 getInstance() {
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
