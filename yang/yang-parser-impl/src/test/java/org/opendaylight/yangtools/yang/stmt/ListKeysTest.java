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

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ListKeysTest {

    @Test
    public void correctListKeysTest() throws ReactorException {

        final StatementStreamSource yangFile = sourceForResource("/list-keys-test/correct-list-keys-test.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void incorrectListKeysTest1() throws IOException, YangSyntaxErrorException, URISyntaxException {

        final StatementStreamSource yangFile = sourceForResource("/list-keys-test/incorrect-list-keys-test.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        try {
            reactor.buildEffective();
            fail("effective build should fail due to list instead of leaf referenced in list key");
        } catch (Exception e) {
            assertTrue(e instanceof ReactorException);
            assertTrue(e.getCause().getMessage().startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"));
        }
    }

    @Test
    public void incorrectListKeysTest2() throws IOException, YangSyntaxErrorException, URISyntaxException {

        final StatementStreamSource yangFile = sourceForResource("/list-keys-test/incorrect-list-keys-test2.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        try {
            reactor.buildEffective();
            fail("effective build should fail due to missing leaf referenced in list key");
        } catch (Exception e) {
            assertTrue(e instanceof ReactorException);
            assertTrue(e.getCause().getMessage().startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"));
        }
    }

    @Test
    public void incorrectListKeysTest3() throws IOException, YangSyntaxErrorException, URISyntaxException {

        final StatementStreamSource yangFile = sourceForResource("/list-keys-test/incorrect-list-keys-test3.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        try {
            reactor.buildEffective();
            fail("effective build should fail due to list instead of leaf in grouping referenced in list key");
        } catch (Exception e) {
            assertTrue(e instanceof ReactorException);
            assertTrue(e.getCause().getMessage().startsWith("Key 'grp_list' misses node 'grp_list'"));
        }
    }

    @Test
    public void incorrectListKeysTest4() throws IOException, YangSyntaxErrorException, URISyntaxException {

        final StatementStreamSource yangFile = sourceForResource("/list-keys-test/incorrect-list-keys-test4.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        try {
            reactor.buildEffective();
            fail("effective build should fail due to list instead of leaf in grouping augmented to list referenced " +
                    "in list key");
        } catch (Exception e) {
            assertTrue(e instanceof ReactorException);
            assertTrue(e.getCause().getMessage().startsWith("Key 'grp_leaf' misses node 'grp_leaf'"));
        }
    }
}
