/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.opendaylight.yangtools.yang.stmt.TestUtils.assertThatSystemOutput;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6878Test extends AbstractYangTest {
    @Test
    void testParsingXPathWithYang11Functions() {
        assertThatEffectiveModelLogOutput("/rfc7950/bug6878/foo.yang")
            .doesNotContain("Could not find function: ");
    }

    @Test
    void shouldLogInvalidYang10XPath() {
        assertThatEffectiveModelLogOutput("/rfc7950/bug6878/foo10-invalid.yang")
            .contains("RFC7950 features required in RFC6020 context to parse expression ");
    }

    @Test
    void shouldLogInvalidYang10XPath2() {
        assertThatEffectiveModelLogOutput("/rfc7950/bug6878/foo10-invalid-2.yang")
            .contains("RFC7950 features required in RFC6020 context to parse expression ");
    }

    private static AbstractStringAssert<?> assertThatEffectiveModelLogOutput(final String yangFile) {
        return assertThatSystemOutput(() -> assertEffectiveModel(yangFile));
    }
}
