/*
 * Copyright (c) 2017 Opendaylight.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;

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
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)when statement is not allowed in "
            + "(incorrect-list-keys-test?revision=2017-02-06)a2 leaf statement which is specified as a list key.";
        assertSourceException(startsWith(exceptionMessage), "/rfc7950/list-keys-test/incorrect-list-keys-test.yang");
    }

    @Test
    void invalidListLeafKeyTest2() {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
            + "(incorrect-list-keys-test1?revision=2017-02-06)b leaf statement which is specified as a list key.";
        assertSourceException(startsWith(exceptionMessage), "/rfc7950/list-keys-test/incorrect-list-keys-test1.yang");
    }

    @Test
    void invalidListUsesLeafKeyTest() {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
            + "(incorrect-list-keys-test2?revision=2017-02-06)a1 leaf statement which is specified as a list key.";
        assertSourceException(startsWith(exceptionMessage), "/rfc7950/list-keys-test/incorrect-list-keys-test2.yang");
    }

    @Test
    void invalidListUsesLeafKeyTest1() {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)when statement is not allowed in "
            + "(incorrect-list-keys-test3?revision=2017-02-06)a2 leaf statement which is specified as a list key.";
        assertSourceException(startsWith(exceptionMessage), "/rfc7950/list-keys-test/incorrect-list-keys-test3.yang");
    }

    @Test
    void invalidListUsesLeafKeyTest2() {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
            + "(incorrect-list-keys-test4?revision=2017-02-06)a1 leaf statement which is specified as a list key.";
        assertSourceException(startsWith(exceptionMessage), "/rfc7950/list-keys-test/incorrect-list-keys-test4.yang");
    }

    @Test
    void invalidListUsesRefineLeafKeyTest() {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
            + "(incorrect-list-keys-test5?revision=2017-02-06)a1 leaf statement which is specified as a list key.";
        assertSourceException(startsWith(exceptionMessage), "/rfc7950/list-keys-test/incorrect-list-keys-test5.yang");
    }
}
