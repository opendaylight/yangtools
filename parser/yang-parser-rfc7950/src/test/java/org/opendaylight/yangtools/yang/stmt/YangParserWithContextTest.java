/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class YangParserWithContextTest {
    private static final XMLNamespace T1_NS = XMLNamespace.of("urn:simple.demo.test1");
    private static final XMLNamespace T2_NS = XMLNamespace.of("urn:simple.demo.test2");
    private static final XMLNamespace T3_NS = XMLNamespace.of("urn:simple.demo.test3");
    private static final Revision REV = Revision.of("2013-06-18");

    private static final StatementStreamSource BAR = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource BAZ = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource FOO = sourceForResource("/model/foo.yang");
    private static final StatementStreamSource SUBFOO = sourceForResource("/model/subfoo.yang");

    private static final StatementStreamSource[] IETF = new StatementStreamSource[]{
        sourceForResource("/ietf/iana-afn-safi@2012-06-04.yang"),
        sourceForResource("/ietf/iana-if-type@2012-06-05.yang"),
        sourceForResource("/ietf/iana-timezones@2012-07-09.yang"),
        sourceForResource("/ietf/ietf-inet-types@2010-09-24.yang"),
        sourceForResource("/ietf/ietf-yang-types@2010-09-24.yang"),
        sourceForResource("/ietf/network-topology@2013-07-12.yang"),
        sourceForResource("/ietf/network-topology@2013-10-21.yang")};

    @Test
    void testTypeFromContext() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IETF)
            .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
            .addSource(sourceForResource("/context-test/test1.yang"))
            .buildEffective();

        final var module = context.findModule("test1", Revision.of("2013-06-18")).orElseThrow();
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            module.getDataChildByName(QName.create(module.getQNameModule(), "id")));

        final var leafType = assertInstanceOf(Uint16TypeDefinition.class, leaf.getType());
        QName qname = leafType.getQName();
        assertEquals(XMLNamespace.of("urn:simple.demo.test1"), qname.getNamespace());
        assertEquals(Revision.ofNullable("2013-06-18"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        final var leafBaseType = leafType.getBaseType();
        qname = leafBaseType.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-inet-types"), qname.getNamespace());
        assertEquals(Revision.ofNullable("2010-09-24"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        final var dscpExt = assertInstanceOf(Uint8TypeDefinition.class,
            TestUtils.findTypedef(module.getTypeDefinitions(), "dscp-ext"));
        final var ranges = dscpExt.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();
        assertEquals(1, ranges.size());
        final var range = ranges.iterator().next();
        assertEquals(Uint8.ZERO, range.lowerEndpoint());
        assertEquals(Uint8.valueOf(63), range.upperEndpoint());
    }

    @Test
    void testUsesFromContext() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(BAZ, FOO, BAR, SUBFOO, sourceForResource("/context-test/test2.yang"))
            .buildEffective();

        final var testModule = context.findModule("test2", Revision.of("2013-06-18")).orElseThrow();
        final var contextModule = context.findModules(XMLNamespace.of("urn:opendaylight.baz")).iterator().next();
        assertNotNull(contextModule);
        final var groupings = contextModule.getGroupings();
        assertEquals(1, groupings.size());
        final var grouping = groupings.iterator().next();

        // get node containing uses
        final var peer = assertInstanceOf(ContainerSchemaNode.class,
            testModule.dataChildByName(QName.create(testModule.getQNameModule(), "peer")));
        final var destination = assertInstanceOf(ContainerSchemaNode.class,
            peer.dataChildByName(QName.create(testModule.getQNameModule(), "destination")));

        // check uses
        final var uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final var data_u = assertInstanceOf(AnyxmlSchemaNode.class,
            destination.dataChildByName(QName.create(testModule.getQNameModule(), "data")));
        assertTrue(data_u.isAddedByUses());

        final var data_g = assertInstanceOf(AnyxmlSchemaNode.class,
            grouping.dataChildByName(QName.create(contextModule.getQNameModule(), "data")));
        assertFalse(data_g.isAddedByUses());
        assertNotEquals(data_u, data_g);

        final var how_u = assertInstanceOf(ChoiceSchemaNode.class,
            destination.dataChildByName(QName.create(testModule.getQNameModule(), "how")));
        assertTrue(how_u.isAddedByUses());

        final var how_g = assertInstanceOf(ChoiceSchemaNode.class,
            grouping.dataChildByName(QName.create(contextModule.getQNameModule(), "how")));
        assertFalse(how_g.isAddedByUses());
        assertNotEquals(how_u, how_g);

        final var address_u = assertInstanceOf(LeafSchemaNode.class,
            destination.dataChildByName(QName.create(testModule.getQNameModule(), "address")));
        assertTrue(address_u.isAddedByUses());

        final var address_g = assertInstanceOf(LeafSchemaNode.class,
            grouping.dataChildByName(QName.create(contextModule.getQNameModule(), "address")));
        assertFalse(address_g.isAddedByUses());
        assertNotEquals(address_u, address_g);

        final var port_u = assertInstanceOf(ContainerSchemaNode.class,
            destination.dataChildByName(QName.create(testModule.getQNameModule(), "port")));
        assertTrue(port_u.isAddedByUses());

        final var port_g = assertInstanceOf(ContainerSchemaNode.class,
            grouping.dataChildByName(QName.create(contextModule.getQNameModule(), "port")));
        assertNotNull(port_g);
        assertFalse(port_g.isAddedByUses());
        assertNotEquals(port_u, port_g);

        final var addresses_u = assertInstanceOf(ListSchemaNode.class,
            destination.dataChildByName(QName.create(testModule.getQNameModule(), "addresses")));
        assertNotNull(addresses_u);
        assertTrue(addresses_u.isAddedByUses());

        final var addresses_g = assertInstanceOf(ListSchemaNode.class,
            grouping.dataChildByName(QName.create(contextModule.getQNameModule(), "addresses")));
        assertNotNull(addresses_g);
        assertFalse(addresses_g.isAddedByUses());
        assertNotEquals(addresses_u, addresses_g);

        // grouping defined by 'uses'
        final var groupings_u = destination.getGroupings();
        assertEquals(0, groupings_u.size());

        // grouping defined in 'grouping' node
        final var groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final var grouping_g = groupings_g.iterator().next();
        assertFalse(grouping_g.isAddedByUses());
    }

    @Test
    void testUsesRefineFromContext() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(BAZ, FOO, BAR, SUBFOO, sourceForResource("/context-test/test2.yang"))
            .buildEffective();

        final var module = context.findModule("test2", Revision.of("2013-06-18")).orElseThrow();
        final var peer = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "peer")));
        final var destination = assertInstanceOf(ContainerSchemaNode.class,
            peer.dataChildByName(QName.create(module.getQNameModule(), "destination")));
        final var usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        final UsesNode usesNode = usesNodes.iterator().next();

        // test grouping path
        assertEquals(QName.create(XMLNamespace.of("urn:opendaylight.baz"), Revision.of("2013-02-27"), "target"),
            usesNode.getSourceGrouping().getQName());

        // test refine
        final var refines = usesNode.getRefines();
        assertEquals(List.of(
            Descendant.of(QName.create(T2_NS, REV, "address")),
            Descendant.of(QName.create(T2_NS, REV, "port")),
            Descendant.of(QName.create(T2_NS, REV, "addresses"))),
            List.copyOf(refines));

        // leaf address
        final var refineLeaf = assertInstanceOf(LeafSchemaNode.class,
            destination.dataChildByName(QName.create(T2_NS, REV, "address")));
        assertEquals(Optional.of("description of address defined by refine"), refineLeaf.getDescription());
        assertEquals(Optional.of("address reference added by refine"), refineLeaf.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineLeaf.effectiveConfig());
        assertTrue(refineLeaf.isMandatory());
        final var leafMustConstraints = refineLeaf.getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        final var leafMust = leafMustConstraints.iterator().next();
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.getXpath().toString());

        // container port
        final var refineContainer = assertInstanceOf(ContainerSchemaNode.class,
            destination.dataChildByName(QName.create(T2_NS, REV, "port")));
        final var mustConstraints = refineContainer.getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals(Optional.of("description of port defined by refine"), refineContainer.getDescription());
        assertEquals(Optional.of("port reference added by refine"), refineContainer.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineContainer.effectiveConfig());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        final var refineList = assertInstanceOf(ListSchemaNode.class,
            destination.dataChildByName(QName.create(T2_NS, REV, "addresses")));
        assertEquals(Optional.of("description of addresses defined by refine"), refineList.getDescription());
        assertEquals(Optional.of("addresses reference added by refine"), refineList.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineList.effectiveConfig());
        final var constraint = refineList.getElementCountConstraint().orElseThrow();
        assertEquals(MinElementsArgument.of(2), constraint.getMinElements());
        assertEquals(12, constraint.getMaxElements());
    }

    @Test
    void testIdentity() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IETF)
            .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
            .addSource(sourceForResource("/context-test/test3.yang"))
            .buildEffective();

        final var module = context.findModule("test3", Revision.of("2013-06-18")).orElseThrow();
        final var identities = module.getIdentities();
        assertEquals(1, identities.size());

        final var identity = identities.iterator().next();
        final QName idQName = identity.getQName();
        assertEquals(XMLNamespace.of("urn:simple.demo.test3"), idQName.getNamespace());
        assertEquals(Revision.ofNullable("2013-06-18"), idQName.getRevision());
        assertEquals("pt", idQName.getLocalName());

        final var baseIdentity = Iterables.getOnlyElement(identity.getBaseIdentities());
        final QName idBaseQName = baseIdentity.getQName();
        assertEquals(XMLNamespace.of("urn:custom.types.demo"), idBaseQName.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), idBaseQName.getRevision());
        assertEquals("service-type", idBaseQName.getLocalName());
    }

    @Test
    void testUnknownNodes() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IETF)
            .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
            .addSource(sourceForResource("/context-test/test3.yang"))
            .buildEffective();

        final var module = context.findModule("test3", Revision.of("2013-06-18")).orElseThrow();
        final var network = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "network")))
            .asEffectiveStatement().getDeclared();
        final var unknownNodes = network.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownNodes.size());

        final var un = unknownNodes.iterator().next();
        final QName unType = un.statementDefinition().statementName();
        assertEquals(XMLNamespace.of("urn:custom.types.demo"), unType.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), unType.getRevision());
        assertEquals("mountpoint", unType.getLocalName());
        assertEquals("point", un.argument());
    }

    @Test
    void testAugment() throws Exception {
        final var t4 = TestUtils.parseYangSource(
            "/context-augment-test/test1.yang", "/context-augment-test/test2.yang",
            "/context-augment-test/test3.yang", "/context-augment-test/test4.yang")
            .findModules("test4").iterator().next();
        final var interfaces = assertInstanceOf(ContainerSchemaNode.class,
            t4.dataChildByName(QName.create(t4.getQNameModule(), "interfaces")));
        final var ifEntry = assertInstanceOf(ListSchemaNode.class,
            interfaces.dataChildByName(QName.create(t4.getQNameModule(), "ifEntry")));

        // test augmentation process
        final var augmentHolder = assertInstanceOf(ContainerSchemaNode.class,
            ifEntry.dataChildByName(QName.create(T3_NS, REV, "augment-holder")));
        assertInstanceOf(LeafSchemaNode.class,
            augmentHolder.dataChildByName(QName.create(T2_NS, REV, "ds0ChannelNumber")));
        assertInstanceOf(LeafSchemaNode.class, augmentHolder.dataChildByName(QName.create(T2_NS, REV, "interface-id")));
        assertInstanceOf(LeafListSchemaNode.class,
            augmentHolder.dataChildByName(QName.create(T2_NS, REV, "higher-layer-if")));
        final var schemas = assertInstanceOf(ContainerSchemaNode.class,
            augmentHolder.dataChildByName(QName.create(T2_NS, REV, "schemas")));
        assertInstanceOf(LeafSchemaNode.class, schemas.dataChildByName(QName.create(T1_NS, REV, "id")));

        // test augment target after augmentation: check if it is same instance
        final var ifEntryAfterAugment = assertInstanceOf(ListSchemaNode.class,
            interfaces.dataChildByName(QName.create(t4.getQNameModule(), "ifEntry")));
        assertSame(ifEntry, ifEntryAfterAugment);
    }

    @Test
    void testDeviation() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/model/bar.yang"))
            .addSource(sourceForResource("/context-test/deviation-test.yang"))
            .buildEffective();

        final var testModule = context.findModule("deviation-test", Revision.of("2013-02-27")).orElseThrow();
        final var deviations = testModule.getDeviations();
        assertEquals(1, deviations.size());
        final var dev = deviations.iterator().next();

        assertEquals(Optional.of("system/user ref"), dev.getReference());

        final var expectedNS = XMLNamespace.of("urn:opendaylight.bar");
        final var expectedRev = Revision.of("2013-07-03");

        assertEquals(
            Absolute.of(
                QName.create(expectedNS, expectedRev, "interfaces"),
                QName.create(expectedNS, expectedRev, "ifEntry")),
            dev.getTargetPath());
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
    }
}
