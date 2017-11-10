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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class YangParserWithContextTest {
    private static final URI T1_NS = URI.create("urn:simple.demo.test1");
    private static final URI T2_NS = URI.create("urn:simple.demo.test2");
    private static final URI T3_NS = URI.create("urn:simple.demo.test3");
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
    public void testTypeFromContext() throws ReactorException {
        final SchemaContext context = DefaultReactors.defaultReactor().newBuild()
                .addSources(IETF)
                .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
                .addSource(sourceForResource("/context-test/test1.yang"))
                .buildEffective();

        final Module module = context.findModule("test1", Revision.of("2013-06-18")).get();
        final LeafSchemaNode leaf = (LeafSchemaNode) module.getDataChildByName(QName.create(module.getQNameModule(),
                "id"));

        assertTrue(leaf.getType() instanceof Uint16TypeDefinition);
        final Uint16TypeDefinition leafType = (Uint16TypeDefinition) leaf.getType();
        QName qname = leafType.getQName();
        assertEquals(URI.create("urn:simple.demo.test1"), qname.getNamespace());
        assertEquals(Revision.ofNullable("2013-06-18"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        final Uint16TypeDefinition leafBaseType = leafType.getBaseType();
        qname = leafBaseType.getQName();
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:ietf-inet-types"), qname.getNamespace());
        assertEquals(Revision.ofNullable("2010-09-24"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        final Uint8TypeDefinition dscpExt = (Uint8TypeDefinition) TestUtils.findTypedef(module.getTypeDefinitions(),
            "dscp-ext");
        final Set<? extends Range<?>> ranges = dscpExt.getRangeConstraint().get().getAllowedRanges().asRanges();
        assertEquals(1, ranges.size());
        final Range<?> range = ranges.iterator().next();
        assertEquals((short)0, range.lowerEndpoint());
        assertEquals((short)63, range.upperEndpoint());
    }

    @Test
    public void testUsesFromContext() throws ReactorException {
        final SchemaContext context = DefaultReactors.defaultReactor().newBuild()
                .addSources(BAZ, FOO, BAR, SUBFOO, sourceForResource("/context-test/test2.yang"))
                .buildEffective();

        final Module testModule = context.findModule("test2", Revision.of("2013-06-18")).get();
        final Module contextModule = context.findModules(URI.create("urn:opendaylight.baz")).iterator().next();
        assertNotNull(contextModule);
        final Set<GroupingDefinition> groupings = contextModule.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();

        // get node containing uses
        final ContainerSchemaNode peer = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "peer"));
        final ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                testModule.getQNameModule(), "destination"));

        // check uses
        final Set<UsesNode> uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final AnyXmlSchemaNode data_u = (AnyXmlSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "data"));
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        final AnyXmlSchemaNode data_g = (AnyXmlSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "data"));
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));

        final ChoiceSchemaNode how_u = (ChoiceSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "how"));
        assertNotNull(how_u);
        assertTrue(how_u.isAddedByUses());

        final ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "how"));
        assertNotNull(how_g);
        assertFalse(how_g.isAddedByUses());
        assertFalse(how_u.equals(how_g));

        final LeafSchemaNode address_u = (LeafSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "address"));
        assertNotNull(address_u);
        assertTrue(address_u.isAddedByUses());

        final LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "address"));
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertFalse(address_u.equals(address_g));

        final ContainerSchemaNode port_u = (ContainerSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "port"));
        assertNotNull(port_u);
        assertTrue(port_u.isAddedByUses());

        final ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "port"));
        assertNotNull(port_g);
        assertFalse(port_g.isAddedByUses());
        assertFalse(port_u.equals(port_g));

        final ListSchemaNode addresses_u = (ListSchemaNode) destination.getDataChildByName(QName.create(
                testModule.getQNameModule(), "addresses"));
        assertNotNull(addresses_u);
        assertTrue(addresses_u.isAddedByUses());

        final ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName(QName.create(
                contextModule.getQNameModule(), "addresses"));
        assertNotNull(addresses_g);
        assertFalse(addresses_g.isAddedByUses());
        assertFalse(addresses_u.equals(addresses_g));

        // grouping defined by 'uses'
        final Set<GroupingDefinition> groupings_u = destination.getGroupings();
        assertEquals(1, groupings_u.size());
        final GroupingDefinition grouping_u = groupings_u.iterator().next();
        assertTrue(grouping_u.isAddedByUses());

        // grouping defined in 'grouping' node
        final Set<GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final GroupingDefinition grouping_g = groupings_g.iterator().next();
        assertFalse(grouping_g.isAddedByUses());
        assertFalse(grouping_u.equals(grouping_g));

        final List<UnknownSchemaNode> nodes_u = destination.getUnknownSchemaNodes();
        assertEquals(1, nodes_u.size());
        final UnknownSchemaNode node_u = nodes_u.get(0);
        assertTrue(node_u.isAddedByUses());

        final List<UnknownSchemaNode> nodes_g = grouping.getUnknownSchemaNodes();
        assertEquals(1, nodes_g.size());
        final UnknownSchemaNode node_g = nodes_g.get(0);
        assertFalse(node_g.isAddedByUses());
        assertFalse(node_u.equals(node_g));
    }

    @Test
    public void testUsesRefineFromContext() throws ReactorException {
        final SchemaContext context = DefaultReactors.defaultReactor().newBuild()
                .addSources(BAZ, FOO, BAR, SUBFOO, sourceForResource("/context-test/test2.yang"))
                .buildEffective();

        final Module module = context.findModule("test2", Revision.of("2013-06-18")).get();
        final ContainerSchemaNode peer = (ContainerSchemaNode) module.getDataChildByName(QName.create(
                module.getQNameModule(), "peer"));
        final ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                module.getQNameModule(), "destination"));
        final Set<UsesNode> usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        final UsesNode usesNode = usesNodes.iterator().next();

        // test grouping path
        final List<QName> path = new ArrayList<>();
        final QName qname = QName.create(URI.create("urn:opendaylight.baz"), Revision.of("2013-02-27"), "target");
        path.add(qname);
        final SchemaPath expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, usesNode.getGroupingPath());

        // test refine
        final Map<SchemaPath, SchemaNode> refines = usesNode.getRefines();
        assertEquals(3, refines.size());

        LeafSchemaNode refineLeaf = null;
        ContainerSchemaNode refineContainer = null;
        ListSchemaNode refineList = null;
        for (final Map.Entry<SchemaPath, SchemaNode> entry : refines.entrySet()) {
            final SchemaNode value = entry.getValue();
            if (value instanceof LeafSchemaNode) {
                refineLeaf = (LeafSchemaNode) value;
            } else if (value instanceof ContainerSchemaNode) {
                refineContainer = (ContainerSchemaNode) value;
            } else if (value instanceof ListSchemaNode) {
                refineList = (ListSchemaNode) value;
            }
        }

        // leaf address
        assertNotNull(refineLeaf);
        assertEquals("address", refineLeaf.getQName().getLocalName());
        assertEquals(Optional.of("description of address defined by refine"), refineLeaf.getDescription());
        assertEquals(Optional.of("address reference added by refine"), refineLeaf.getReference());
        assertFalse(refineLeaf.isConfiguration());
        assertTrue(refineLeaf.isMandatory());
        final Collection<MustDefinition> leafMustConstraints = refineLeaf.getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        final MustDefinition leafMust = leafMustConstraints.iterator().next();
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.toString());

        // container port
        assertNotNull(refineContainer);
        final Collection<MustDefinition> mustConstraints = refineContainer.getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals(Optional.of("description of port defined by refine"), refineContainer.getDescription());
        assertEquals(Optional.of("port reference added by refine"), refineContainer.getReference());
        assertFalse(refineContainer.isConfiguration());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        assertNotNull(refineList);
        assertEquals(Optional.of("description of addresses defined by refine"), refineList.getDescription());
        assertEquals(Optional.of("addresses reference added by refine"), refineList.getReference());
        assertFalse(refineList.isConfiguration());
        final ElementCountConstraint constraint = refineList.getElementCountConstraint().get();
        assertEquals(2, constraint.getMinElements().intValue());
        assertEquals(12, constraint.getMaxElements().intValue());
    }

    @Test
    public void testIdentity() throws ReactorException {
        final SchemaContext context = DefaultReactors.defaultReactor().newBuild()
                .addSources(IETF)
                .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
                .addSource(sourceForResource("/context-test/test3.yang"))
                .buildEffective();

        final Module module = context.findModule("test3", Revision.of("2013-06-18")).get();
        final Set<IdentitySchemaNode> identities = module.getIdentities();
        assertEquals(1, identities.size());

        final IdentitySchemaNode identity = identities.iterator().next();
        final QName idQName = identity.getQName();
        assertEquals(URI.create("urn:simple.demo.test3"), idQName.getNamespace());
        assertEquals(Revision.ofNullable("2013-06-18"), idQName.getRevision());
        assertEquals("pt", idQName.getLocalName());

        final IdentitySchemaNode baseIdentity = Iterables.getOnlyElement(identity.getBaseIdentities());
        final QName idBaseQName = baseIdentity.getQName();
        assertEquals(URI.create("urn:custom.types.demo"), idBaseQName.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), idBaseQName.getRevision());
        assertEquals("service-type", idBaseQName.getLocalName());
    }

    @Test
    public void testUnknownNodes() throws ReactorException {
        final SchemaContext context = DefaultReactors.defaultReactor().newBuild()
                .addSources(IETF)
                .addSource(sourceForResource("/types/custom-types-test@2012-04-04.yang"))
                .addSource(sourceForResource("/context-test/test3.yang"))
                .buildEffective();

        final Module module = context.findModule("test3", Revision.of("2013-06-18")).get();
        final ContainerSchemaNode network = (ContainerSchemaNode) module.getDataChildByName(QName.create(
                module.getQNameModule(), "network"));
        final List<UnknownSchemaNode> unknownNodes = network.getUnknownSchemaNodes();
        assertEquals(1, unknownNodes.size());

        final UnknownSchemaNode un = unknownNodes.get(0);
        final QName unType = un.getNodeType();
        assertEquals(URI.create("urn:custom.types.demo"), unType.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), unType.getRevision());
        assertEquals("mountpoint", unType.getLocalName());
        assertEquals("point", un.getNodeParameter());
        assertNotNull(un.getExtensionDefinition());
    }

    @Test
    public void testAugment() throws ReactorException {
        final StatementStreamSource resource = sourceForResource("/context-augment-test/test4.yang");
        final StatementStreamSource test1 = sourceForResource("/context-augment-test/test1.yang");
        final StatementStreamSource test2 = sourceForResource("/context-augment-test/test2.yang");
        final StatementStreamSource test3 = sourceForResource("/context-augment-test/test3.yang");

        final SchemaContext context = TestUtils.parseYangSources(resource, test1, test2, test3);
        final Module t4 = TestUtils.findModule(context, "test4").get();
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) t4.getDataChildByName(QName.create(
                t4.getQNameModule(), "interfaces"));
        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(t4.getQNameModule(),
                "ifEntry"));

        // test augmentation process
        final ContainerSchemaNode augmentHolder = (ContainerSchemaNode) ifEntry.getDataChildByName(QName.create(T3_NS,
                REV, "augment-holder"));
        assertNotNull(augmentHolder);
        final DataSchemaNode ds0 = augmentHolder.getDataChildByName(QName.create(T2_NS, REV, "ds0ChannelNumber"));
        assertNotNull(ds0);
        final DataSchemaNode interfaceId = augmentHolder.getDataChildByName(QName.create(T2_NS, REV, "interface-id"));
        assertNotNull(interfaceId);
        final DataSchemaNode higherLayerIf = augmentHolder.getDataChildByName(QName.create(T2_NS, REV,
                "higher-layer-if"));
        assertNotNull(higherLayerIf);
        final ContainerSchemaNode schemas = (ContainerSchemaNode) augmentHolder.getDataChildByName(QName.create(T2_NS,
                REV, "schemas"));
        assertNotNull(schemas);
        assertNotNull(schemas.getDataChildByName(QName.create(T1_NS, REV, "id")));

        // test augment target after augmentation: check if it is same instance
        final ListSchemaNode ifEntryAfterAugment = (ListSchemaNode) interfaces.getDataChildByName(QName.create(
                t4.getQNameModule(), "ifEntry"));
        assertTrue(ifEntry == ifEntryAfterAugment);
    }

    @Test
    public void testDeviation() throws ReactorException {
        final SchemaContext context = DefaultReactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/model/bar.yang"))
                .addSource(sourceForResource("/context-test/deviation-test.yang"))
                .buildEffective();

        final Module testModule = context.findModule("deviation-test", Revision.of("2013-02-27")).get();
        final Set<Deviation> deviations = testModule.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation dev = deviations.iterator().next();

        assertEquals(Optional.of("system/user ref"), dev.getReference());

        final URI expectedNS = URI.create("urn:opendaylight.bar");
        final Revision expectedRev = Revision.of("2013-07-03");
        final List<QName> path = new ArrayList<>();
        path.add(QName.create(expectedNS, expectedRev, "interfaces"));
        path.add(QName.create(expectedNS, expectedRev, "ifEntry"));
        final SchemaPath expectedPath = SchemaPath.create(path, true);

        assertEquals(expectedPath, dev.getTargetPath());
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
    }

}
