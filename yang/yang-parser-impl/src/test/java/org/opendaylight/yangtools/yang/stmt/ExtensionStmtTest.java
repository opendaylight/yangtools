/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ExtensionStmtTest {

    private static final StatementStreamSource EXT_DEF_MODULE = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource EXT_DEF_MODULE2 = sourceForResource(
        "/semantic-statement-parser/ext-typedef.yang");
    private static final StatementStreamSource EXT_USE_MODULE = sourceForResource(
        "/semantic-statement-parser/ext-use.yang");

    @Test
    public void testExtensionDefinition() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(EXT_DEF_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("bar", null);
        assertNotNull(testModule);

        assertEquals(1, testModule.getExtensionSchemaNodes().size());

        final List<ExtensionDefinition> extensions = testModule.getExtensionSchemaNodes();
        final ExtensionDefinition extension = extensions.get(0);
        assertEquals("opendaylight", extension.getQName().getLocalName());
        assertEquals("name", extension.getArgument());
        assertTrue(extension.isYinElement());
    }

    @Test
    public void testExtensionUsage() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(EXT_DEF_MODULE2, EXT_USE_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule1 = result.findModuleByName("ext-typedef", null);
        assertNotNull(testModule1);

        assertEquals(1, testModule1.getExtensionSchemaNodes().size());

        final List<ExtensionDefinition> extensions = testModule1.getExtensionSchemaNodes();
        final ExtensionDefinition extensionDefinition = extensions.get(0);

        final Module testModule2 = result.findModuleByName("ext-use", null);
        assertNotNull(testModule2);

        final LeafSchemaNode leaf = (LeafSchemaNode) testModule2.getDataChildByName(QName.create(testModule2.getQNameModule(), "value"));
        assertNotNull(leaf);

        assertEquals(1, leaf.getUnknownSchemaNodes().size());
        final List<UnknownSchemaNode> unknownNodes = leaf.getUnknownSchemaNodes();
        final UnknownSchemaNode extensionUse = unknownNodes.get(0);
        assertEquals(extensionDefinition.getQName().getLocalName(), extensionUse.getExtensionDefinition().getQName()
                .getLocalName());
        assertEquals(extensionDefinition.getArgument(), extensionUse.getExtensionDefinition().getArgument());

        assertEquals("key:value", extensionUse.getNodeParameter());
    }
}
