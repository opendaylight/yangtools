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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ThirdPartyExtensionPluginTest {
    private static final String NS = "urn:opendaylight:yang:extension:third-party";
    private static final String REV = "2016-06-09";

    @Test
    public void test() throws FileNotFoundException, URISyntaxException, ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = CustomInferencePipeline.CUSTOM_REACTOR.newBuild();
        final FileInputStream yangFile = new FileInputStream(new File(getClass().getResource("/plugin-test/foo.yang")
                .toURI()));
        reactor.addSource(new YangStatementSourceImpl(yangFile));

        final EffectiveSchemaContext schema = reactor.buildEffective();
        final DataSchemaNode dataChildByName = schema.getDataChildByName(QName.create(NS, REV, "root"));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        final ContainerSchemaNode root = (ContainerSchemaNode) dataChildByName;

        final List<UnknownSchemaNode> unknownSchemaNodes = root.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        assertTrue(unknownSchemaNode instanceof ThirdPartyExtensionEffectiveStatementImpl);

        final ThirdPartyExtensionEffectiveStatementImpl thirdPartyExtensionStmt = (ThirdPartyExtensionEffectiveStatementImpl) unknownSchemaNode;
        assertEquals("Third-party namespace test.", thirdPartyExtensionStmt.getValueFromNamespace());
        assertEquals("plugin test", thirdPartyExtensionStmt.argument());
    }
}
