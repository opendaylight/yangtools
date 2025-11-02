/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6867BasicTest extends AbstractYangTest {
    @Test
    void valid10Test() {
        assertEffectiveModel("/rfc7950/basic-test/valid-10.yang");
    }

    @Test
    void valid11Test() {
        assertEffectiveModel("/rfc7950/basic-test/valid-11.yang");
    }

    @Test
    void invalid10Test() {
        assertThat(assertException(InvalidSubstatementException.class, "/rfc7950/basic-test/invalid-10.yang")
            .getMessage()).startsWith("statement container does not allow notification substatements: 1 present [at ");
    }

    @Test
    void invalid11Test() {
        assertThat(assertException(InvalidSubstatementException.class, "/rfc7950/basic-test/invalid-11.yang")
            .getMessage()).startsWith("statement container does not allow rpc substatements: 1 present [at ");
    }

    @Test
    void anyData11Test() {
        assertEffectiveModel("/rfc7950/basic-test/anydata-11.yang");
    }

    @Test
    void anyData10Test() {
        assertSourceException(startsWith("anydata is not a YANG statement or use of extension"),
            "/rfc7950/basic-test/anydata-10.yang");
    }

    @Test
    void yangModelTest() {
        assertEffectiveModelDir("/rfc7950/model");
    }

    @Test
    void unsupportedVersionTest() {
        assertExtractorException(startsWith("Invalid YANG version 2.3"),
            "/rfc7950/basic-test/unsupported-version.yang");
    }
}
