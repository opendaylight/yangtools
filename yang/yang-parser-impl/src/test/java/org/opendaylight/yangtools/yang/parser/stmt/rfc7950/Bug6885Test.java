/*
 * Copyright (c) 2017 Opendaylight.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Bug6885Test {

    private static final Logger LOG = LoggerFactory.getLogger(Bug6885Test.class);

    @Test
    public void valid10Test() throws ReactorException, FileNotFoundException, URISyntaxException {
        // Yang 1.0 allows "if-feature" and "when" on list keys
        final SchemaContext schemaContext =
                StmtTestUtils.parseYangSource("/rfc7950/list-keys-test/correct-list-keys-test.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void invalidListLeafKeyTest1() {
        testForWhen("/rfc7950/list-keys-test/incorrect-list-keys-test.yang");
    }

    @Test
    public void invalidListLeafKeyTest2() {
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test1.yang");
    }

    @Test
    public void invalidListUsesLeafKeyTest() {
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test2.yang");
    }

    @Test
    public void invalidListUsesLeafKeyTest1() {
        testForWhen("/rfc7950/list-keys-test/incorrect-list-keys-test3.yang");
    }

    @Test
    public void invalidListUsesLeafKeyTest2() {
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test4.yang");
    }

    private void testForIfFeature(String yangSrcPath) {
        try {
            StmtTestUtils.parseYangSource(yangSrcPath);
            fail("Test must fail: IF-FEATURE substatement is not allowed in LIST keys");
        } catch (ReactorException | FileNotFoundException | URISyntaxException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("RFC7950: IF-FEATURE not allowed on LIST KEY"));
        }
    }

    private void testForWhen(String yangSrcPath) {
        try {
            StmtTestUtils.parseYangSource(yangSrcPath);
            fail("Test must fail: WHEN substatement is not allowed in LIST keys");
        } catch (ReactorException | FileNotFoundException | URISyntaxException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("RFC7950: WHEN not allowed on LIST KEY"));
        }
    }
}
