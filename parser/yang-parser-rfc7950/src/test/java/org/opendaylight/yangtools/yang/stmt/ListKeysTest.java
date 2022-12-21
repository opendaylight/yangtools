/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class ListKeysTest extends AbstractYangTest {
    @Test
    void correctListKeysTest() {
        assertEffectiveModel("/list-keys-test/correct-list-keys-test.yang");
    }

    @Test
    void incorrectListKeysTest1() {
        assertInferenceException(startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"),
            "/list-keys-test/incorrect-list-keys-test.yang");
    }

    @Test
    void incorrectListKeysTest2() {
        assertInferenceException(startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"),
            "/list-keys-test/incorrect-list-keys-test2.yang");
    }

    @Test
    void incorrectListKeysTest3() {
        assertInferenceException(startsWith("Key 'grp_list' misses node 'grp_list'"),
            "/list-keys-test/incorrect-list-keys-test3.yang");
    }

    @Test
    void incorrectListKeysTest4()  {
        assertInferenceException(startsWith("Key 'grp_leaf' misses node 'grp_leaf'"),
            "/list-keys-test/incorrect-list-keys-test4.yang");
    }
}
