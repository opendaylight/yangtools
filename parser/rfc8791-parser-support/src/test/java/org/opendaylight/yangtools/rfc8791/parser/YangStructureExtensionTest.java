/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.StructureSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class YangStructureExtensionTest {

    private static final QNameModule EXAMPLE_MODULE = QNameModule.of("urn:example:example-module");
    private static final QNameModule EXAMPLE_MODULE_FOO = QNameModule.of("foo");
    private static final QNameModule EXAMPLE_MODULE_BAR = QNameModule.of("bar");
    private static final QNameModule EXAMPLE_MODULE_BAZ = QNameModule.of("baz");
    private static final QName EXAMPLE_ADDRESS_BOOK = QName.create(EXAMPLE_MODULE, "address-book");
    private static final QName EXAMPLE_ADDRESS = QName.create(EXAMPLE_MODULE, "address");
    private static final QName EXAMPLE_FOO_DATA = QName.create(EXAMPLE_MODULE_FOO, "foo-data");

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                new Rfc8791ParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testStructure() throws Exception {
        final var schemaContext = reactor.newBuild()
                .addLibSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                                "/ietf-yang-structure-ext@2020-06-17.yang")))
                .addSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/example.yang")),
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/example-module-aug.yang")))
                .buildEffective();
        assertNotNull(schemaContext);

        final Collection<? extends ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(2, extensions.size());

        final Module foo = schemaContext.findModule(EXAMPLE_MODULE).get();
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        StructureSchemaNode addressBook = null;
        for (final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            assertTrue(unknownSchemaNode instanceof StructureSchemaNode);
            final StructureSchemaNode yangDataSchemaNode = (StructureSchemaNode) unknownSchemaNode;
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
    public void testAugmentResolving() throws Exception {
        // test inspired by AugmentTest.testAugmentResolving
        // acording to RFC8791
        final var schemaContext = reactor.newBuild()
                .addLibSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                                "/ietf-yang-structure-ext@2020-06-17.yang")))
                .addSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo.yang")),
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/bar.yang")))
                .buildEffective();
        assertNotNull(schemaContext);

        final Collection<? extends ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(2, extensions.size());

        final Module foo = schemaContext.findModule(EXAMPLE_MODULE_FOO).get();
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
    public void testAugmentParsing() throws Exception {
        final var schemaContext = reactor.newBuild()
                .addLibSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                                "/ietf-yang-structure-ext@2020-06-17.yang")))
                .addSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo.yang")),
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/bar.yang")))
                .buildEffective();
        assertNotNull(schemaContext);

        final Collection<? extends ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(2, extensions.size());

        final Module bar = schemaContext.findModule(EXAMPLE_MODULE_BAR).get();

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
    public void testConfigStatementBeingIgnoredInStructure() throws Exception {
        final var schemaContext = reactor.newBuild()
                .addLibSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                                "/ietf-yang-structure-ext@2020-06-17.yang")))
                .addSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo.yang")),
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/bar.yang")))
                .buildEffective();
        assertNotNull(schemaContext);

        final Module foo = schemaContext.findModule(EXAMPLE_MODULE_FOO).get();
        final var unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        final var structureNode = (StructureSchemaNode) unknownSchemaNodes.iterator().next();
        assertEquals(structureNode.getQName(), EXAMPLE_FOO_DATA);

        final var fooCont = (ContainerSchemaNode) structureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con"));
        assertNotNull(fooCont);
        assertEquals(Optional.empty(), fooCont.effectiveConfig());
    }

    @Test
    public void testIgnoreStructure() throws Exception {
        // TODO: in AugmentStructure argument needs to be valid path even when ignored, is it correct behavior?
        //   if not foo.yang don't need to be added as resource or imported in baz.yang
        final var schemaContext = reactor.newBuild()
                .addLibSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                                "/ietf-yang-structure-ext@2020-06-17.yang")))
                .addSources(
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo.yang")),
                        YangStatementStreamSource.create(YangTextSchemaSource.forResource("/baz.yang")))
                .buildEffective();
        assertNotNull(schemaContext);

        final Module baz = schemaContext.findModule(EXAMPLE_MODULE_BAZ).get();
        final var cont = (ContainerSchemaNode) baz.findDataChildByName(
                QName.create(baz.getQNameModule(), "cont")).get();
        assertNotNull(cont);
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = cont.getUnknownSchemaNodes();
        assertEquals(0, unknownSchemaNodes.size());
    }
}
