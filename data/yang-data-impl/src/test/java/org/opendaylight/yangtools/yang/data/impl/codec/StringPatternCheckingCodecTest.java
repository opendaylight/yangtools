/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class StringPatternCheckingCodecTest {
    @Test
    public void testStringPatternCheckingCodec() {
        final var schemaContext = YangParserTestUtils.parseYangResource("/string-pattern-checking-codec-test.yang");
        assertNotNull(schemaContext);

        final var testModuleQName = QNameModule.create(XMLNamespace.of("string-pattern-checking-codec-test"));

        final var testModule = schemaContext.findModules("string-pattern-checking-codec-test").iterator().next();
        final var testContainer = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModuleQName, "test-container"));

        final var testLeaf = (LeafSchemaNode) testContainer.getDataChildByName(
                QName.create(testModuleQName, "string-leaf-with-valid-pattern"));

        final var codec = getCodec(testLeaf.getType(), StringCodec.class);
        assertNotNull(codec);
        assertEquals("ABCD", codec.serialize("ABCD"));
        assertEquals("ABCD", codec.deserialize("ABCD"));

        final var ex = assertThrows(IllegalArgumentException.class, () -> codec.deserialize("abcd"));
        assertEquals("Value 'abcd' does not match regular expression '[A-Z]+'", ex.getMessage());
    }
}
