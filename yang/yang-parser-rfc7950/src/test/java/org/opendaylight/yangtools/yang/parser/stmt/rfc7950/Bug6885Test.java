/*
 * Copyright (c) 2017 Opendaylight.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6885Test {

    @Test
    public void validYang10Test() throws Exception {
        // Yang 1.0 allows "if-feature" and "when" on list keys
        final SchemaContext schemaContext =
                StmtTestUtils.parseYangSource("/rfc7950/list-keys-test/correct-list-keys-test.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void invalidListLeafKeyTest1() throws Exception {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)when statement is not allowed in "
                + "(incorrect-list-keys-test?revision=2017-02-06)a2 leaf statement which is specified as a list key.";
        testForWhen("/rfc7950/list-keys-test/incorrect-list-keys-test.yang", exceptionMessage);
    }

    @Test
    public void invalidListLeafKeyTest2() throws Exception {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
                + "(incorrect-list-keys-test1?revision=2017-02-06)b leaf statement which is specified as a list key.";
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test1.yang", exceptionMessage);
    }

    @Test
    public void invalidListUsesLeafKeyTest() throws Exception {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
                + "(incorrect-list-keys-test2?revision=2017-02-06)a1 leaf statement which is specified as a list key.";
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test2.yang", exceptionMessage);
    }

    @Test
    public void invalidListUsesLeafKeyTest1() throws Exception {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)when statement is not allowed in "
                + "(incorrect-list-keys-test3?revision=2017-02-06)a2 leaf statement which is specified as a list key.";
        testForWhen("/rfc7950/list-keys-test/incorrect-list-keys-test3.yang", exceptionMessage);
    }

    @Test
    public void invalidListUsesLeafKeyTest2() throws Exception {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
                + "(incorrect-list-keys-test4?revision=2017-02-06)a1 leaf statement which is specified as a list key.";
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test4.yang", exceptionMessage);
    }

    @Test
    public void invalidListUsesRefineLeafKeyTest() throws Exception {
        final String exceptionMessage = "(urn:ietf:params:xml:ns:yang:yin:1)if-feature statement is not allowed in "
                + "(incorrect-list-keys-test5?revision=2017-02-06)a1 leaf statement which is specified as a list key.";
        testForIfFeature("/rfc7950/list-keys-test/incorrect-list-keys-test5.yang", exceptionMessage);
    }

    private static void testForIfFeature(final String yangSrcPath, final String exMsg) throws URISyntaxException,
            SourceException, IOException, YangSyntaxErrorException {
        try {
            StmtTestUtils.parseYangSource(yangSrcPath);
            fail("Test must fail: IF-FEATURE substatement is not allowed in LIST keys");
        } catch (final ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(exMsg));
        }
    }

    private static void testForWhen(final String yangSrcPath, final String exMsg) throws URISyntaxException,
            SourceException, IOException, YangSyntaxErrorException {
        try {
            StmtTestUtils.parseYangSource(yangSrcPath);
            fail("Test must fail: WHEN substatement is not allowed in LIST keys");
        } catch (final ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(exMsg));
        }
    }
}
