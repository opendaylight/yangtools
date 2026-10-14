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
            .startsWith("Key 'test1_key1 test1_key2' component 'test1_key2' refers to a child list [at ")
            .endsWith(":6:5]");
    }

    @Test
    void incorrectListKeysTest2() {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test2.yang")
            .startsWith(
                "Key 'test1_key1 test1_key2' misses node 'test1_key2' in list '(incorrect-list-keys-test2)test1' [at ")
            .endsWith(":6:5]");
    }

    @Test
    void incorrectListKeysTest3() {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test3.yang")
            .startsWith("Key 'grp_list' component 'grp_list' refers to a child list [at ")
            .endsWith(":17:5]");
    }

    @Test
    void incorrectListKeysTest4()  {
        assertInferenceExceptionMessage("/list-keys-test/incorrect-list-keys-test4.yang")
            .startsWith("Key 'grp_leaf' component 'grp_leaf' refers to a child list [at ")
            .endsWith(":11:5]");
    }
}
