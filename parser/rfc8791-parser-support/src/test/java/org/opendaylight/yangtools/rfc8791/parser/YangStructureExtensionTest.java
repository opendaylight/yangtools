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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class YangStructureExtensionTest {
    private static final QNameModule EXAMPLE_MODULE = QNameModule.of("urn:example:example-module");
    private static final QNameModule EXAMPLE_MODULE_FOO = QNameModule.of("foo");
    private static final QNameModule EXAMPLE_MODULE_BAR = QNameModule.of("bar");
    private static final QNameModule EXAMPLE_MODULE_BAZ = QNameModule.of("baz");
    private static final QName EXAMPLE_ADDRESS_BOOK = QName.create(EXAMPLE_MODULE, "address-book");
    private static final QName EXAMPLE_ADDRESS = QName.create(EXAMPLE_MODULE, "address");
    private static final QName EXAMPLE_FOO_DATA = QName.create(EXAMPLE_MODULE_FOO, "foo-data");
    private static final YangTextToIRSourceTransformer TRANSFORMER = YangIRSourceModule.provideTextToIR();

    private static CrossSourceStatementReactor reactor;
    private static YangIRSource sxSource;
    private static YangIRSource fooSource;
    private static YangIRSource barSource;
    private static YangIRSource bazSource;

    @BeforeAll
    static void beforeAll() throws Exception {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                new Rfc8791ParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
        sxSource = TRANSFORMER.transformSource(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/ietf-yang-structure-ext@2020-06-17.yang")));
        fooSource = TRANSFORMER.transformSource(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/foo.yang")));
        barSource = TRANSFORMER.transformSource(new URLYangTextSource(
            YangStructureExtensionTest.class.getResource("/bar.yang")));
        bazSource = TRANSFORMER.transformSource(new URLYangTextSource(
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
            .addLibSource(sxSource)
            .addSource(TRANSFORMER.transformSource(new URLYangTextSource(
                YangStructureExtensionTest.class.getResource("/example.yang"))))
            .addSource(TRANSFORMER.transformSource(new URLYangTextSource(
                YangStructureExtensionTest.class.getResource("/example-module-aug.yang"))))
            .buildEffective();
        assertNotNull(modelContext);

        final var extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var foo = modelContext.findModuleStatement(EXAMPLE_MODULE).orElseThrow();
        final var addressBook = foo.findFirstEffectiveSubstatement(StructureEffectiveStatement.class).orElseThrow();
        assertEquals(EXAMPLE_ADDRESS_BOOK, addressBook.argument());
        var address = assertInstanceOf(ListSchemaNode.class,
            addressBook.toDataNodeContainer().dataChildByName(EXAMPLE_ADDRESS));
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
            .addLibSource(sxSource)
            .addSource(fooSource)
            .addSource(barSource)
            .buildEffective();
        assertNotNull(modelContext);

        final Collection<? extends ExtensionDefinition> extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var foo = modelContext.findModuleStatement(EXAMPLE_MODULE_FOO).orElseThrow();
        final var structureNode = foo.findFirstEffectiveSubstatement(StructureEffectiveStatement.class).orElseThrow();
       assertEquals(EXAMPLE_FOO_DATA, structureNode.argument());

        final var fooCont = assertInstanceOf(ContainerSchemaNode.class, structureNode.toDataNodeContainer()
                .dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con")));
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
            .addLibSource(sxSource)
            .addSource(fooSource)
            .addSource(barSource)
            .buildEffective();
        assertNotNull(modelContext);

        final var extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var bar = modelContext.findModuleStatement(EXAMPLE_MODULE_BAR).orElseThrow();
        final var augmentStructureNode = bar.findFirstEffectiveSubstatement(AugmentStructureEffectiveStatement.class)
            .orElseThrow().toDataNodeContainer();

        final var leaf1 = augmentStructureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf1"));
        assertNotNull(leaf1);
        final var leaf2 = augmentStructureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf2"));
        assertNotNull(leaf2);
    }

    @Test
    void testConfigStatementBeingIgnoredInStructure() throws Exception {
        final var modelContext = reactor.newBuild()
            .addLibSource(sxSource)
            .addSource(fooSource)
            .addSource(barSource)
            .buildEffective();
        assertNotNull(modelContext);

        final var foo = modelContext.findModuleStatement(EXAMPLE_MODULE_FOO).orElseThrow();
        final var structureNode = foo.findFirstEffectiveSubstatement(StructureEffectiveStatement.class).orElseThrow();
        assertEquals(EXAMPLE_FOO_DATA, structureNode.argument());

        final var fooCont = assertInstanceOf(ContainerSchemaNode.class,
            structureNode.toDataNodeContainer().dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con")));
        assertNotNull(fooCont);
        assertEquals(Optional.empty(), fooCont.effectiveConfig());
    }

    @Test
    void testIgnoreStructure() throws Exception {
        // TODO: in AugmentStructure argument needs to be valid path even when ignored, is it correct behavior?
        //   if not foo.yang don't need to be added as resource or imported in baz.yang
        final var modelContext = reactor.newBuild()
            .addLibSource(sxSource)
            .addSource(fooSource)
            .addSource(bazSource)
            .buildEffective();
        assertNotNull(modelContext);

        final var baz = modelContext.findModuleStatement(EXAMPLE_MODULE_BAZ).orElseThrow();
        final var cont = assertInstanceOf(ContainerEffectiveStatement.class,
            baz.findDataTreeNode(QName.create(baz.localQNameModule(), "cont")));
        assertEquals(List.of(), cont.effectiveSubstatements());
    }
}
