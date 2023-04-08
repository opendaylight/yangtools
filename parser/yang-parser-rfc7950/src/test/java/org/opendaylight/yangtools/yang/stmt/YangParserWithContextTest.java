/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class YangParserWithContextTest {
    private static final XMLNamespace T1_NS = XMLNamespace.of("urn:simple.demo.test1");
    private static final XMLNamespace T2_NS = XMLNamespace.of("urn:simple.demo.test2");
    private static final XMLNamespace T3_NS = XMLNamespace.of("urn:simple.demo.test3");
    private static final Revision REV = Revision.of("2013-06-18");

    private static final StatementStreamSource BAR = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource BAZ = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource FOO = sourceForResource("/model/foo.yang");
    private static final StatementStreamSource SUBFOO = sourceForResource("/model/subfoo.yang");

    private static final StatementStreamSource[] IETF = new StatementStreamSource[] {
            sourceForResource("/ietf/iana-afn-safi@2012-06-04.yang"),
            sourceForResource("/ietf/iana-if-type@2012-06-05.yang"),
            sourceForResource("/ietf/iana-timezones@2012-07-09.yang"),
            sourceForResource("/ietf/ietf-inet-types@2010-09-24.yang"),
            sourceForResource("/ietf/ietf-yang-types@2010-09-24.yang"),
            sourceForResource("/ietf/network-topology@2013-07-12.yang"),
            sourceForResource("/ietf/network-topology@2013-10-21.yang") };

    @Test
    public void testTypeFromContext() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(IETF)
                .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
                .addSource(sourceForResource("/context-test/test1.yang"))
                .buildEffective();

        final var module = context.findModule("test1", Revision.of("2013-06-18")).orElseThrow();
        final var leaf = (LeafSchemaNode) module.getDataChildByName(QName.create(module.getQNameModule(), "id"));

        assertTrue(leaf.getType() instanceof Uint16TypeDefinition);
        final var leafType = (Uint16TypeDefinition) leaf.getType();
        QName qname = leafType.getQName();
        assertEquals(XMLNamespace.of("urn:simple.demo.test1"), qname.getNamespace());
        assertEquals(Revision.ofNullable("2013-06-18"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        final var leafBaseType = leafType.getBaseType();
        qname = leafBaseType.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-inet-types"), qname.getNamespace());
        assertEquals(Revision.ofNullable("2010-09-24"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        final var dscpExt = (Uint8TypeDefinition) TestUtils.findTypedef(module.getTypeDefinitions(), "dscp-ext");
        final var ranges = dscpExt.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();
        assertEquals(1, ranges.size());
        final Range<?> range = ranges.iterator().next();
        assertEquals(Uint8.valueOf(0), range.lowerEndpoint());
        assertEquals(Uint8.valueOf(63), range.upperEndpoint());
    }

    @Test
    public void testUsesFromContext() throws Exception {
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
        final var peer = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "peer"));
        final var destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                testModule.getQNameModule(), "destination"));

        // check uses
        final var uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final var data_u = (AnyxmlSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "data"));
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        final var data_g = (AnyxmlSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "data"));
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));

        final var how_u = (ChoiceSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "how"));
        assertNotNull(how_u);
        assertTrue(how_u.isAddedByUses());

        final var how_g = (ChoiceSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "how"));
        assertNotNull(how_g);
        assertFalse(how_g.isAddedByUses());
        assertFalse(how_u.equals(how_g));

        final var address_u = (LeafSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "address"));
        assertNotNull(address_u);
        assertTrue(address_u.isAddedByUses());

        final var address_g = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "address"));
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertFalse(address_u.equals(address_g));

        final var port_u = (ContainerSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "port"));
        assertNotNull(port_u);
        assertTrue(port_u.isAddedByUses());

        final var port_g = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "port"));
        assertNotNull(port_g);
        assertFalse(port_g.isAddedByUses());
        assertFalse(port_u.equals(port_g));

        final var addresses_u = (ListSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "addresses"));
        assertNotNull(addresses_u);
        assertTrue(addresses_u.isAddedByUses());

        final var addresses_g = (ListSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "addresses"));
        assertNotNull(addresses_g);
        assertFalse(addresses_g.isAddedByUses());
        assertFalse(addresses_u.equals(addresses_g));

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
    public void testUsesRefineFromContext() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(BAZ, FOO, BAR, SUBFOO, sourceForResource("/context-test/test2.yang"))
                .buildEffective();

        final var module = context.findModule("test2", Revision.of("2013-06-18")).orElseThrow();
        final var peer = (ContainerSchemaNode) module.getDataChildByName(QName.create(
                module.getQNameModule(), "peer"));
        final var destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                module.getQNameModule(), "destination"));
        final var usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        final UsesNode usesNode = usesNodes.iterator().next();

        // test grouping path
        assertEquals(QName.create(XMLNamespace.of("urn:opendaylight.baz"), Revision.of("2013-02-27"), "target"),
            usesNode.getSourceGrouping().getQName());

        // test refine
        final var refines = usesNode.getRefines();
        assertEquals(3, refines.size());

        LeafSchemaNode refineLeaf = null;
        ContainerSchemaNode refineContainer = null;
        ListSchemaNode refineList = null;
        for (var entry : refines.entrySet()) {
            final SchemaNode value = entry.getValue();
            if (value instanceof LeafSchemaNode leaf) {
                refineLeaf = leaf;
            } else if (value instanceof ContainerSchemaNode container) {
                refineContainer = container;
            } else if (value instanceof ListSchemaNode list) {
                refineList = list;
            }
        }

        // leaf address
        assertNotNull(refineLeaf);
        assertEquals("address", refineLeaf.getQName().getLocalName());
        assertEquals(Optional.of("description of address defined by refine"), refineLeaf.getDescription());
        assertEquals(Optional.of("address reference added by refine"), refineLeaf.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineLeaf.effectiveConfig());
        assertTrue(refineLeaf.isMandatory());
        final var leafMustConstraints = refineLeaf.getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        final var leafMust = leafMustConstraints.iterator().next();
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.getXpath().toString());

        // container port
        assertNotNull(refineContainer);
        final var mustConstraints = refineContainer.getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals(Optional.of("description of port defined by refine"), refineContainer.getDescription());
        assertEquals(Optional.of("port reference added by refine"), refineContainer.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineContainer.effectiveConfig());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        assertNotNull(refineList);
        assertEquals(Optional.of("description of addresses defined by refine"), refineList.getDescription());
        assertEquals(Optional.of("addresses reference added by refine"), refineList.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineList.effectiveConfig());
        final var constraint = refineList.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 2, constraint.getMinElements());
        assertEquals((Object) 12, constraint.getMaxElements());
    }

    @Test
    public void testIdentity() throws Exception {
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
    public void testUnknownNodes() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(IETF)
                .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
                .addSource(sourceForResource("/context-test/test3.yang"))
                .buildEffective();

        final var module = context.findModule("test3", Revision.of("2013-06-18")).orElseThrow();
        final var network = ((ContainerSchemaNode) module.getDataChildByName(
            QName.create(module.getQNameModule(), "network"))).asEffectiveStatement().getDeclared();
        final var unknownNodes = network.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownNodes.size());

        final var un = unknownNodes.iterator().next();
        final QName unType = un.statementDefinition().getStatementName();
        assertEquals(XMLNamespace.of("urn:custom.types.demo"), unType.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), unType.getRevision());
        assertEquals("mountpoint", unType.getLocalName());
        assertEquals("point", un.argument());
    }

    @Test
    public void testAugment() throws Exception {
        final var t4 = TestUtils.parseYangSource(
            "/context-augment-test/test1.yang", "/context-augment-test/test2.yang",
            "/context-augment-test/test3.yang", "/context-augment-test/test4.yang")
            .findModules("test4").iterator().next();
        final var interfaces = (ContainerSchemaNode) t4.getDataChildByName(QName.create(
                t4.getQNameModule(), "interfaces"));
        final var ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(t4.getQNameModule(),
                "ifEntry"));

        // test augmentation process
        final var augmentHolder = (ContainerSchemaNode) ifEntry.getDataChildByName(QName.create(T3_NS,
                REV, "augment-holder"));
        assertNotNull(augmentHolder);
        final var ds0 = augmentHolder.getDataChildByName(QName.create(T2_NS, REV, "ds0ChannelNumber"));
        assertNotNull(ds0);
        final var interfaceId = augmentHolder.getDataChildByName(QName.create(T2_NS, REV, "interface-id"));
        assertNotNull(interfaceId);
        final var higherLayerIf = augmentHolder.getDataChildByName(QName.create(T2_NS, REV, "higher-layer-if"));
        assertNotNull(higherLayerIf);
        final var schemas = (ContainerSchemaNode) augmentHolder.getDataChildByName(QName.create(T2_NS, REV, "schemas"));
        assertNotNull(schemas);
        assertNotNull(schemas.getDataChildByName(QName.create(T1_NS, REV, "id")));

        // test augment target after augmentation: check if it is same instance
        final var ifEntryAfterAugment = (ListSchemaNode) interfaces.getDataChildByName(QName.create(
                t4.getQNameModule(), "ifEntry"));
        assertTrue(ifEntry == ifEntryAfterAugment);
    }

    @Test
    public void testDeviation() throws Exception {
        final var context = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/model/bar.yang"))
                .addSource(sourceForResource("/context-test/deviation-test.yang"))
                .buildEffective();

        final var testModule = context.findModule("deviation-test", Revision.of("2013-02-27")).orElseThrow();
        final var deviations = testModule.getDeviations();
        assertEquals(1, deviations.size());
        final var dev = deviations.iterator().next();

        assertEquals(Optional.of("system/user ref"), dev.getReference());

        final XMLNamespace expectedNS = XMLNamespace.of("urn:opendaylight.bar");
        final Revision expectedRev = Revision.of("2013-07-03");

        assertEquals(Absolute.of(
            QName.create(expectedNS, expectedRev, "interfaces"), QName.create(expectedNS, expectedRev, "ifEntry")),
            dev.getTargetPath());
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
    }
}
