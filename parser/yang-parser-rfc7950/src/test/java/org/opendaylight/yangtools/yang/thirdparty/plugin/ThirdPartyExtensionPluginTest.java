/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class ThirdPartyExtensionPluginTest {
    private static final String NS = "urn:opendaylight:yang:extension:third-party";
    private static final String REV = "2016-06-09";

    @Test
    void test() throws Exception {
        final BuildAction reactor = RFC7950Reactors.defaultReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new ThirdPartyExtensionSupport(YangParserConfiguration.DEFAULT))
            .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, ThirdPartyNamespace.BEHAVIOUR)
            .build()
            .newBuild();
        reactor.addSource(StmtTestUtils.sourceForResource("/plugin-test/foo.yang"));

        final EffectiveModelContext schema = reactor.buildEffective();
        final DataSchemaNode dataChildByName = schema.getDataChildByName(QName.create(NS, REV, "root"));

        final ContainerSchemaNode root = assertInstanceOf(ContainerSchemaNode.class, dataChildByName);

        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = root.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        final ThirdPartyExtensionEffectiveStatement thirdPartyExtensionStmt =
            assertInstanceOf(ThirdPartyExtensionEffectiveStatement.class, unknownSchemaNode);
        assertEquals("Third-party namespace test.", thirdPartyExtensionStmt.getValueFromNamespace());
        assertEquals("plugin test", thirdPartyExtensionStmt.argument());
    }
}
