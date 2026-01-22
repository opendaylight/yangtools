/*
 * Copyright (c) 2017 Opendaylight.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6885Test extends AbstractYangTest {
    @Test
    void validYang10Test() {
        // Yang 1.0 allows "if-feature" and "when" on list keys
        assertEffectiveModel("/rfc7950/list-keys-test/correct-list-keys-test.yang");
    }

    @Test
    void invalidListLeafKeyTest1() {
        assertThat(assertSourceException("/rfc7950/list-keys-test/incorrect-list-keys-test.yang").getMessage())
            .startsWith("""
                leaf statement (incorrect-list-keys-test?revision=2017-02-06)a2 is a key in list statement \
                (incorrect-list-keys-test?revision=2017-02-06)invalid-list-a: it cannot be conditional on when \
                statement [at """)
            .endsWith(":14:9]");
    }

    @Test
    void invalidListLeafKeyTest2() {
        assertThat(assertSourceException("/rfc7950/list-keys-test/incorrect-list-keys-test1.yang").getMessage())
            .startsWith("""
                leaf statement (incorrect-list-keys-test1?revision=2017-02-06)b is a key in list statement \
                (incorrect-list-keys-test1?revision=2017-02-06)invalid-list-b: it cannot be conditional on if-feature \
                statement [at """)
            .endsWith(":12:9]");
    }

    @Test
    void invalidListUsesLeafKeyTest() {
        assertThat(assertSourceException("/rfc7950/list-keys-test/incorrect-list-keys-test2.yang").getMessage())
            .startsWith("""
                leaf statement (incorrect-list-keys-test2?revision=2017-02-06)a1 is a key in list statement \
                (incorrect-list-keys-test2?revision=2017-02-06)invalid-list-a1: it cannot be conditional on if-feature \
                statement [at """)
            .endsWith(":11:9]");
    }

    @Test
    void invalidListUsesLeafKeyTest1() {
        assertThat(assertSourceException("/rfc7950/list-keys-test/incorrect-list-keys-test3.yang").getMessage())
            .startsWith("""
                leaf statement (incorrect-list-keys-test3?revision=2017-02-06)a2 is a key in list statement \
                (incorrect-list-keys-test3?revision=2017-02-06)invalid-list-a2: it cannot be conditional on when \
                statement [at """)
            .endsWith("15:9]");
    }

    @Test
    void invalidListUsesLeafKeyTest2() {
        assertThat(assertSourceException("/rfc7950/list-keys-test/incorrect-list-keys-test4.yang").getMessage())
            .startsWith("""
                leaf statement (incorrect-list-keys-test4?revision=2017-02-06)a1 is a key in list statement \
                (incorrect-list-keys-test4?revision=2017-02-06)invalid-list-b1: it cannot be conditional on if-feature \
                statement [at """)
            .endsWith("11:9]");
    }

    @Test
    void invalidListUsesRefineLeafKeyTest() {
        assertThat(assertSourceException("/rfc7950/list-keys-test/incorrect-list-keys-test5.yang").getMessage())
            .startsWith("""
                leaf statement (incorrect-list-keys-test5?revision=2017-02-06)a1 is a key in list statement \
                (incorrect-list-keys-test5?revision=2017-02-06)invalid-list-a1: it cannot be conditional on if-feature \
                statement [at """)
            .endsWith(":13:9]");
    }
}
