/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class ThirdPartyExtensionPluginTest {
    private static final String NS = "urn:opendaylight:yang:extension:third-party";
    private static final String REV = "2016-06-09";

    @Test
    public void test() throws URISyntaxException, ReactorException, IOException, YangSyntaxErrorException {
        final CrossSourceStatementReactor.BuildAction reactor = CustomInferencePipeline.CUSTOM_REACTOR.newBuild();
        reactor.addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/plugin-test/foo.yang")));

        final SchemaContext schema = reactor.buildEffective();
        final DataSchemaNode dataChildByName = schema.getDataChildByName(QName.create(NS, REV, "root"));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        final ContainerSchemaNode root = (ContainerSchemaNode) dataChildByName;

        final List<UnknownSchemaNode> unknownSchemaNodes = root.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        assertTrue(unknownSchemaNode instanceof ThirdPartyExtensionEffectiveStatementImpl);

        final ThirdPartyExtensionEffectiveStatementImpl thirdPartyExtensionStmt =
                (ThirdPartyExtensionEffectiveStatementImpl) unknownSchemaNode;
        assertEquals("Third-party namespace test.", thirdPartyExtensionStmt.getValueFromNamespace());
        assertEquals("plugin test", thirdPartyExtensionStmt.argument());
    }
}
