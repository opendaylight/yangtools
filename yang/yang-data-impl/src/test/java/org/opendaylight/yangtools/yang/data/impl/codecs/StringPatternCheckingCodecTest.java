/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringPatternCheckingCodecTest {

    private static final Logger LOG = LoggerFactory.getLogger(StringPatternCheckingCodecTest.class);
    private static final YangStatementSourceImpl TEST_MODULE = new YangStatementSourceImpl
            ("/string-pattern-checking-codec-test.yang", false);

    @Test
    public void testStringPatternCheckingCodec() throws ReactorException, ParseException, URISyntaxException {
        SchemaContext schemaContext = YangParserTestUtils.parseYangSources(TEST_MODULE);
        assertNotNull(schemaContext);

        QNameModule testModuleQName = QNameModule.create(new URI("string-pattern-checking-codec-test"),
                SimpleDateFormatUtil.getRevisionFormat().parse("1970-01-01"));

        Module testModule = schemaContext.findModuleByName("string-pattern-checking-codec-test", null);
        assertNotNull(testModule);

        ContainerSchemaNode testContainer = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModuleQName, "test-container"));
        assertNotNull(testContainer);

        LeafSchemaNode testLeaf = (LeafSchemaNode) testContainer.getDataChildByName(
                QName.create(testModuleQName, "string-leaf-with-valid-pattern"));
        assertNotNull(testLeaf);

        StringCodec<String> codec = getCodec(testLeaf.getType(), StringCodec.class);
        assertNotNull(codec);
        assertEquals("ABCD", codec.serialize("ABCD"));
        assertEquals("ABCD", codec.deserialize("ABCD"));

        try {
            codec.deserialize("abcd");
            fail("Exception should have been thrown.");
        } catch (IllegalArgumentException ex) {
            LOG.debug("IllegalArgumentException was thrown as expected: {}", ex);
            assertTrue(ex.getMessage().contains("is not valid regular expression. [abcd]"));
        }
    }
}
