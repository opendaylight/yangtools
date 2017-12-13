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
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringPatternCheckingCodecTest {

    private static final Logger LOG = LoggerFactory.getLogger(StringPatternCheckingCodecTest.class);

    @Test
    public void testStringPatternCheckingCodec() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(
            "/string-pattern-checking-codec-test.yang");
        assertNotNull(schemaContext);

        final QNameModule testModuleQName = QNameModule.create(URI.create("string-pattern-checking-codec-test"));

        final Module testModule = schemaContext.findModules("string-pattern-checking-codec-test").iterator().next();
        final ContainerSchemaNode testContainer = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModuleQName, "test-container"));
        assertNotNull(testContainer);

        final LeafSchemaNode testLeaf = (LeafSchemaNode) testContainer.getDataChildByName(
                QName.create(testModuleQName, "string-leaf-with-valid-pattern"));
        assertNotNull(testLeaf);

        final StringCodec<String> codec = getCodec(testLeaf.getType(), StringCodec.class);
        assertNotNull(codec);
        assertEquals("ABCD", codec.serialize("ABCD"));
        assertEquals("ABCD", codec.deserialize("ABCD"));

        try {
            codec.deserialize("abcd");
            fail("Exception should have been thrown.");
        } catch (final IllegalArgumentException ex) {
            LOG.debug("IllegalArgumentException was thrown as expected", ex);
            assertEquals("Supplied value does not match the regular expression ^[A-Z]+$.", ex.getMessage());
        }
    }
}
