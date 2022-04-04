/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

public class Bug6867BasicTest extends AbstractYangTest {
    @Test
    public void valid10Test() {
        assertEffectiveModel("/rfc7950/basic-test/valid-10.yang");
    }

    @Test
    public void valid11Test() {
        assertEffectiveModel("/rfc7950/basic-test/valid-11.yang");
    }

    @Test
    public void invalid10Test() {
        assertException(InvalidSubstatementException.class, startsWith("NOTIFICATION is not valid for CONTAINER"),
            "/rfc7950/basic-test/invalid-10.yang");
    }

    @Test
    public void invalid11Test() {
        assertException(InvalidSubstatementException.class, startsWith("RPC is not valid for CONTAINER"),
            "/rfc7950/basic-test/invalid-11.yang");
    }

    @Test
    public void anyData11Test() {
        assertEffectiveModel("/rfc7950/basic-test/anydata-11.yang");
    }

    @Test
    public void anyData10Test() {
        assertSourceException(startsWith("anydata is not a YANG statement or use of extension"),
            "/rfc7950/basic-test/anydata-10.yang");
    }

    @Test
    public void yangModelTest() {
        assertEffectiveModelDir("/rfc7950/model");
    }

    @Test
    public void unsupportedVersionTest() {
        assertSourceException(startsWith("Unsupported YANG version 2.3"),
            "/rfc7950/basic-test/unsupported-version.yang");
    }
}
