/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6643.model.api.AliasEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DefValEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.ObjectIdentifier;
import org.opendaylight.yangtools.rfc6643.model.api.OidEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.parser.dagger.Rfc6643Module;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class IetfYangSmiv2ExtensionPluginTest {
    private static final String NS = "urn:opendaylight:yang:extension:third-party";
    private static final String REV = "2016-06-09";

    @Test
    void testExtensions() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                Rfc6643Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
        final var context = reactor.newBuild(YangIRSourceModule.provideTextToIR())
            .addSource(new URLYangTextSource(IetfYangSmiv2ExtensionPluginTest.class.getResource("/foo.yang")))
            .addSource(new URLYangTextSource(
                IetfYangSmiv2ExtensionPluginTest.class.getResource("/ietf-yang-smiv2.yang")))
            .buildEffective();

        final var foo = context.findModuleStatements("foo").iterator().next();

        assertThat(foo.collectEffectiveSubstatements(AliasEffectiveStatement.class)).hasSize(1).first()
            .satisfies(alias -> {
                assertEquals("ifXTable", alias.argument());
                final var oid = alias.findFirstEffectiveSubstatement(OidEffectiveStatement.class).orElseThrow();
                assertEquals(ObjectIdentifier.of(1, 3, 6, 1, 2, 1, 31, 1, 1), oid.argument());
            });

        final var root = assertInstanceOf(ContainerSchemaNode.class,
            context.dataChildByName(QName.create(NS, REV, "root")));

        assertThat(root.asEffectiveStatement().collectEffectiveSubstatements(OidEffectiveStatement.class))
            .hasSize(1).first().satisfies(oid -> {
                assertEquals(ObjectIdentifier.of(1), oid.argument());
            });

        final var ifStackEntry = assertInstanceOf(ListSchemaNode.class,
            root.dataChildByName(QName.create(NS, REV, "ifStackEntry")));

        assertThat(ifStackEntry.asEffectiveStatement().findFirstEffectiveSubstatement(OidEffectiveStatement.class))
            .get().satisfies(oidSchema -> {
                assertEquals(ObjectIdentifier.of(1, 1), oidSchema.argument());
            });

        assertThat(ifStackEntry.asEffectiveStatement().findFirstEffectiveSubstatement(ImpliedEffectiveStatement.class))
            .get().satisfies(impliedSchema -> {
                assertEquals("ifStackHigherLayer", impliedSchema.argument());
            });

        final var ifStackHigherLayer = assertInstanceOf(LeafSchemaNode.class,
            ifStackEntry.dataChildByName(QName.create(NS, REV, "ifStackHigherLayer"))).asEffectiveStatement();

        assertThat(ifStackHigherLayer.findFirstEffectiveSubstatement(MaxAccessEffectiveStatement.class))
            .get().satisfies(maxAccess -> {
                assertEquals(MaxAccess.NOT_ACCESSIBLE, maxAccess.argument());
            });

        final var testNumberLeaf = assertInstanceOf(LeafSchemaNode.class,
            ifStackEntry.getDataChildByName(QName.create(NS, REV, "test-number"))).asEffectiveStatement();

        final var testNumberLeafSubs = testNumberLeaf.effectiveSubstatements();
        assertEquals(5, testNumberLeafSubs.size());

        assertThat(testNumberLeafSubs.get(2)).isInstanceOfSatisfying(MaxAccessEffectiveStatement.class, maxAccess -> {
            assertEquals(MaxAccess.NOT_ACCESSIBLE, maxAccess.argument());
        });
        assertThat(testNumberLeafSubs.get(3)).isInstanceOfSatisfying(SubIdEffectiveStatement.class, subid -> {
            assertEquals(Uint32.TWO, subid.argument());
        });
        assertThat(testNumberLeafSubs.get(4)).isInstanceOfSatisfying(DefValEffectiveStatement.class, defVal -> {
            assertEquals("0", defVal.argument());
        });
    }
}
