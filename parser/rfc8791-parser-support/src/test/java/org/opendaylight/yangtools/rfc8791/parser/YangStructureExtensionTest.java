/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.StructureSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

class YangStructureExtensionTest {
    private static final QNameModule EXAMPLE_MODULE = QNameModule.of("urn:example:example-module");
    private static final QNameModule EXAMPLE_MODULE_FOO = QNameModule.of("foo");
    private static final QNameModule EXAMPLE_MODULE_BAR = QNameModule.of("bar");
    private static final QNameModule EXAMPLE_MODULE_BAZ = QNameModule.of("baz");
    private static final QName EXAMPLE_ADDRESS_BOOK = QName.create(EXAMPLE_MODULE, "address-book");
    private static final QName EXAMPLE_ADDRESS = QName.create(EXAMPLE_MODULE, "address");
    private static final QName EXAMPLE_FOO_DATA = QName.create(EXAMPLE_MODULE_FOO, "foo-data");

    private static CrossSourceStatementReactor reactor;
    private static YangStatementStreamSource sxSource;
    private static YangStatementStreamSource fooSource;
    private static YangStatementStreamSource barSource;
    private static YangStatementStreamSource bazSource;

    @BeforeAll
    static void beforeAll() throws Exception {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                new Rfc8791ParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
        sxSource = YangStatementStreamSource.create(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/ietf-yang-structure-ext@2020-06-17.yang")));
        fooSource = YangStatementStreamSource.create(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/foo.yang")));
        barSource = YangStatementStreamSource.create(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/bar.yang")));
        bazSource = YangStatementStreamSource.create(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/baz.yang")));
    }

    @AfterAll
    static void afterAll() {
        bazSource = null;
        barSource = null;
        fooSource = null;
        sxSource = null;
        reactor = null;
    }

    @Test
    void testStructure() throws Exception {
        final var modelContext = reactor.newBuild()
            .addLibSources(sxSource)
            .addSources(
                YangStatementStreamSource.create(new URLYangTextSource(
                    YangStructureExtensionTest.class.getResource("/example.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    YangStructureExtensionTest.class.getResource("/example-module-aug.yang"))))
            .buildEffective();
        assertNotNull(modelContext);

        final var extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var foo = modelContext.findModule(EXAMPLE_MODULE).get();
        final var unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        StructureSchemaNode addressBook = null;
        for (var unknownSchemaNode : unknownSchemaNodes) {
            final var yangDataSchemaNode = assertInstanceOf(StructureSchemaNode.class, unknownSchemaNode);
            if (EXAMPLE_ADDRESS_BOOK.equals(yangDataSchemaNode.getQName())) {
                addressBook = yangDataSchemaNode;
            }
        }
        assertNotNull(addressBook);
        var address = (ListSchemaNode) addressBook.dataChildByName(EXAMPLE_ADDRESS);
        assertNotNull(address);
        assertEquals(5, address.getChildNodes().size());
        var last = address.dataChildByName(QName.create(EXAMPLE_MODULE,"last"));
        assertNotNull(last);
    }

    @Test
    void testAugmentResolving() throws Exception {
        // test inspired by AugmentTest.testAugmentResolving
        // acording to RFC8791
        final var modelContext = reactor.newBuild()
            .addLibSources(sxSource)
            .addSources(fooSource, barSource)
            .buildEffective();
        assertNotNull(modelContext);

        final Collection<? extends ExtensionDefinition> extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final Module foo = modelContext.findModule(EXAMPLE_MODULE_FOO).get();
        final var unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        final var structureNode = (StructureSchemaNode) unknownSchemaNodes.iterator().next();
        assertEquals(structureNode.getQName(), EXAMPLE_FOO_DATA);

        final var fooCont = (ContainerSchemaNode) structureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con"));
        assertNotNull(fooCont);
        final var leaf1 = fooCont
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf1"));
        final var leaf2 = fooCont
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf2"));
        // test fails here
        // leafs from augmentation in bar.yang are not present in foo:foo-con
        assertNotNull(leaf1);
        assertNotNull(leaf2);
    }

    @Test
    void testAugmentParsing() throws Exception {
        final var modelContext = reactor.newBuild()
            .addLibSources(sxSource)
            .addSources(fooSource, barSource)
            .buildEffective();
        assertNotNull(modelContext);

        final var extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var bar = modelContext.findModule(EXAMPLE_MODULE_BAR).get();

        final var unknownSchemaNodes = bar.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        final var augmentStructureNode = (AugmentStructureSchemaNode) unknownSchemaNodes.iterator().next();

        final var leaf1 = augmentStructureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf1"));
        final var leaf2 = augmentStructureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf2"));
        assertNotNull(leaf1);
        assertNotNull(leaf2);
    }

    @Test
    void testConfigStatementBeingIgnoredInStructure() throws Exception {
        final var modelContext = reactor.newBuild()
            .addLibSources(sxSource)
            .addSources(fooSource, barSource)
            .buildEffective();
        assertNotNull(modelContext);

        final var foo = modelContext.findModule(EXAMPLE_MODULE_FOO).get();
        final var unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        final var structureNode = assertInstanceOf(StructureSchemaNode.class, unknownSchemaNodes.iterator().next());
        assertEquals(structureNode.getQName(), EXAMPLE_FOO_DATA);

        final var fooCont = (ContainerSchemaNode) structureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con"));
        assertNotNull(fooCont);
        assertEquals(Optional.empty(), fooCont.effectiveConfig());
    }

    @Test
    void testIgnoreStructure() throws Exception {
        // TODO: in AugmentStructure argument needs to be valid path even when ignored, is it correct behavior?
        //   if not foo.yang don't need to be added as resource or imported in baz.yang
        final var modelContext = reactor.newBuild()
            .addLibSources(sxSource)
            .addSources(fooSource, bazSource)
            .buildEffective();
        assertNotNull(modelContext);

        final var baz = modelContext.findModule(EXAMPLE_MODULE_BAZ).get();
        final var cont = (ContainerSchemaNode) baz.findDataChildByName(
                QName.create(baz.getQNameModule(), "cont")).get();
        assertNotNull(cont);
        final var unknownSchemaNodes = cont.getUnknownSchemaNodes();
        assertEquals(0, unknownSchemaNodes.size());
    }
}
