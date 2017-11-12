/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

public class ListKeysTest {

    @Test
    public void correctListKeysTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/list-keys-test/correct-list-keys-test.yang"))
                .buildEffective();
        assertNotNull(result);
    }

    @Test
    public void incorrectListKeysTest1() {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/list-keys-test/incorrect-list-keys-test.yang"));
        try {
            reactor.buildEffective();
            fail("effective build should fail due to list instead of leaf referenced in list key");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"));
        }
    }

    @Test
    public void incorrectListKeysTest2() {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/list-keys-test/incorrect-list-keys-test2.yang"));
        try {
            reactor.buildEffective();
            fail("effective build should fail due to missing leaf referenced in list key");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"));
        }
    }

    @Test
    public void incorrectListKeysTest3() {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/list-keys-test/incorrect-list-keys-test3.yang"));
        try {
            reactor.buildEffective();
            fail("effective build should fail due to list instead of leaf in grouping referenced in list key");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Key 'grp_list' misses node 'grp_list'"));
        }
    }

    @Test
    public void incorrectListKeysTest4()  {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/list-keys-test/incorrect-list-keys-test4.yang"));
        try {
            reactor.buildEffective();
            fail("effective build should fail due to list instead of leaf in grouping augmented to list referenced "
                    + "in list key");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Key 'grp_leaf' misses node 'grp_leaf'"));
        }
    }
}
