/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class ListKeysTest extends AbstractYangTest {
    @Test
    void correctListKeysTest() {
        assertEffectiveModel("/list-keys-test/correct-list-keys-test.yang");
    }

    @Test
    void incorrectListKeysTest1() {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test.yang")
            .startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'");
    }

    @Test
    void incorrectListKeysTest2() {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test2.yang")
            .startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'");
    }

    @Test
    void incorrectListKeysTest3() {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test3.yang")
            .startsWith("Key 'grp_list' misses node 'grp_list'");
    }

    @Test
    void incorrectListKeysTest4()  {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test4.yang")
            .startsWith("Key 'grp_leaf' misses node 'grp_leaf'");
    }
}
