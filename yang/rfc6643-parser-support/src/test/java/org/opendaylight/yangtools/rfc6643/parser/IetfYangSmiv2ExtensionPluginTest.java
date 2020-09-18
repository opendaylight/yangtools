/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc6643.model.api.AliasSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.DefValSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.ObjectIdentifier;
import org.opendaylight.yangtools.rfc6643.model.api.OidSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class IetfYangSmiv2ExtensionPluginTest {
    private static final String NS = "urn:opendaylight:yang:extension:third-party";
    private static final String REV = "2016-06-09";

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.defaultReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, DisplayHintStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, MaxAccessStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, DefValStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, ImpliedStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AliasStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, OidStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, SubIdStatementSupport.getInstance())
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testExtensions() throws ReactorException, YangSyntaxErrorException, IOException {
        SchemaContext schema = createSchema();

        assertTrue(schema.getUnknownSchemaNodes().size() == 1);
        AliasSchemaNode aliasExtEffectStmt = (AliasSchemaNode) schema.getUnknownSchemaNodes().iterator().next();
        assertEquals(aliasExtEffectStmt.getArgument(), "ifXTable");
        assertTrue(aliasExtEffectStmt.getUnknownSchemaNodes().size() == 1);
        OidSchemaNode oidExtEffectStmt = (OidSchemaNode) aliasExtEffectStmt.getUnknownSchemaNodes().iterator().next();
        assertEquals(oidExtEffectStmt.getArgument().toString(), "1.3.6.1.2.1.31.1.1");

        final DataSchemaNode dataChildByName = schema.getDataChildByName(QName.create(NS, REV, "root"));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        final ContainerSchemaNode root = (ContainerSchemaNode) dataChildByName;
        assertTrue(root.getUnknownSchemaNodes().size() == 1);
        OidSchemaNode oid = (OidSchemaNode) root.getUnknownSchemaNodes().iterator().next();
        assertEquals(oid.getArgument().toString(), "1");

        ListSchemaNode ifStackEntry = (ListSchemaNode) root.getDataChildByName(QName.create(NS, REV, "ifStackEntry"));
        assertTrue(ifStackEntry.getUnknownSchemaNodes().size() == 2);
        ifStackEntry.getUnknownSchemaNodes().forEach(unknownSchemaNode -> {
            if (unknownSchemaNode instanceof OidSchemaNode) {
                assertEquals(((OidSchemaNode) unknownSchemaNode).getArgument().toString(),
                        ObjectIdentifier.forString("1.1").toString());
            } else if (unknownSchemaNode instanceof ImpliedSchemaNode) {
                assertEquals(((ImpliedSchemaNode) unknownSchemaNode).getArgument(), "ifStackHigherLayer");
            }
        });

        LeafSchemaNode ifStackHigherLayer = (LeafSchemaNode)
                ifStackEntry.getDataChildByName(QName.create(NS, REV, "ifStackHigherLayer"));

        ifStackHigherLayer.getUnknownSchemaNodes().forEach(unknownSchemaNode -> {
            if (unknownSchemaNode instanceof MaxAccessSchemaNode) {
                final MaxAccessSchemaNode maxAccess = (MaxAccessSchemaNode) unknownSchemaNode;
                assertEquals(maxAccess.getArgument(), "not-accessible");
            }
        });

        LeafSchemaNode testNumberLeaf = (LeafSchemaNode)
                ifStackEntry.getDataChildByName(QName.create(NS, REV, "test-number"));
        assertTrue(testNumberLeaf.getUnknownSchemaNodes().size() == 3);
        testNumberLeaf.getUnknownSchemaNodes().forEach(unknownSchemaNode -> {
            if (unknownSchemaNode instanceof SubIdSchemaNode) {
                assertEquals(Uint32.TWO, ((SubIdSchemaNode) unknownSchemaNode).getArgument());
            } else if (unknownSchemaNode instanceof DefValSchemaNode) {
                assertEquals("0", ((DefValSchemaNode) unknownSchemaNode).getArgument());
            }
        });
    }

    private static SchemaContext createSchema() throws ReactorException, YangSyntaxErrorException, IOException {
        return reactor.newBuild()
                .addSources(
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                        IetfYangSmiv2ExtensionPluginTest.class, "/foo.yang")),
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                        IetfYangSmiv2ExtensionPluginTest.class, "/ietf-yang-smiv2.yang")))
                .buildEffective();
    }
}
