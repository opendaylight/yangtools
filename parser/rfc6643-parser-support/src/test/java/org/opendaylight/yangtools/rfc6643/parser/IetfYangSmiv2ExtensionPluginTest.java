/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6643.model.api.AliasSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.DefValSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.ObjectIdentifier;
import org.opendaylight.yangtools.rfc6643.model.api.OidSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class IetfYangSmiv2ExtensionPluginTest {
    private static final String NS = "urn:opendaylight:yang:extension:third-party";
    private static final String REV = "2016-06-09";

    @Test
    void testExtensions() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new DisplayHintStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new MaxAccessStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new DefValStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new ImpliedStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new AliasStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new OidStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new SubIdStatementSupport(YangParserConfiguration.DEFAULT))
            .build();
        final var schema = reactor.newBuild()
            .addSources(
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    IetfYangSmiv2ExtensionPluginTest.class, "/foo.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    IetfYangSmiv2ExtensionPluginTest.class, "/ietf-yang-smiv2.yang")))
            .buildEffective();

        assertEquals(1, schema.getUnknownSchemaNodes().size());
        final var aliasExtEffectStmt = assertInstanceOf(AliasSchemaNode.class,
            schema.getUnknownSchemaNodes().iterator().next());
        assertEquals("ifXTable", aliasExtEffectStmt.getArgument());
        assertEquals(1, aliasExtEffectStmt.getUnknownSchemaNodes().size());
        final var oidExtEffectStmt = assertInstanceOf(OidSchemaNode.class,
            aliasExtEffectStmt.getUnknownSchemaNodes().iterator().next());
        assertEquals("1.3.6.1.2.1.31.1.1", oidExtEffectStmt.getArgument().toString());

        final var root = assertInstanceOf(ContainerSchemaNode.class,
            schema.dataChildByName(QName.create(NS, REV, "root")));
        assertEquals(1, root.getUnknownSchemaNodes().size());
        final var oid = assertInstanceOf(OidSchemaNode.class, root.getUnknownSchemaNodes().iterator().next());
        assertEquals("1", oid.getArgument().toString());

        final var ifStackEntry = assertInstanceOf(ListSchemaNode.class,
            root.dataChildByName(QName.create(NS, REV, "ifStackEntry")));
        assertEquals(2, ifStackEntry.getUnknownSchemaNodes().size());
        ifStackEntry.getUnknownSchemaNodes().forEach(unknownSchemaNode -> {
            if (unknownSchemaNode instanceof OidSchemaNode oidSchema) {
                assertEquals(ObjectIdentifier.forString("1.1"), oidSchema.getArgument());
            } else if (unknownSchemaNode instanceof ImpliedSchemaNode impliedSchema) {
                assertEquals("ifStackHigherLayer", impliedSchema.getArgument());
            }
        });

        final var ifStackHigherLayer = assertInstanceOf(LeafSchemaNode.class,
            ifStackEntry.dataChildByName(QName.create(NS, REV, "ifStackHigherLayer")));
        ifStackHigherLayer.getUnknownSchemaNodes().forEach(unknownSchemaNode -> {
            if (unknownSchemaNode instanceof MaxAccessSchemaNode maxAccess) {
                assertEquals(MaxAccess.NOT_ACCESSIBLE, maxAccess.getArgument());
            }
        });

        final var testNumberLeaf = assertInstanceOf(LeafSchemaNode.class,
            ifStackEntry.getDataChildByName(QName.create(NS, REV, "test-number")));
        assertEquals(3, testNumberLeaf.getUnknownSchemaNodes().size());
        testNumberLeaf.getUnknownSchemaNodes().forEach(unknownSchemaNode -> {
            if (unknownSchemaNode instanceof SubIdSchemaNode subId) {
                assertEquals(Uint32.TWO, subId.getArgument());
            } else if (unknownSchemaNode instanceof DefValSchemaNode defVal) {
                assertEquals("0", defVal.getArgument());
            }
        });
    }
}
