/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;

import org.opendaylight.yangtools.yang.stmt.test.StmtTestUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Deviation.Deviate;
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
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class YangParserWithContextTest {
    private final DateFormat simpleDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    private final YangParserImpl parser = new YangParserImpl();

    private static final YangStatementSourceImpl BAR = new YangStatementSourceImpl(
            "/model/bar.yang", false);
    private static final YangStatementSourceImpl BAZ = new YangStatementSourceImpl(
            "/model/baz.yang", false);
    private static final YangStatementSourceImpl FOO = new YangStatementSourceImpl(
            "/model/foo.yang", false);
    private static final YangStatementSourceImpl SUBFOO = new YangStatementSourceImpl(
            "/model/subfoo.yang", false);

    private static final YangStatementSourceImpl[] IETF = new YangStatementSourceImpl[] {
        new YangStatementSourceImpl("/ietf/iana-afn-safi@2012-06-04.yang", false),
        new YangStatementSourceImpl("/ietf/iana-if-type@2012-06-05.yang", false),
        new YangStatementSourceImpl("/ietf/iana-timezones@2012-07-09.yang", false),
        new YangStatementSourceImpl("/ietf/ietf-inet-types@2010-09-24.yang", false),
        new YangStatementSourceImpl("/ietf/ietf-yang-types@2010-09-24.yang", false),
        new YangStatementSourceImpl("/ietf/network-topology@2013-07-12.yang", false),
        new YangStatementSourceImpl("/ietf/network-topology@2013-10-21.yang", false)
    };

    @Test
    public void testTypeFromContext() throws Exception {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        YangStatementSourceImpl types = new YangStatementSourceImpl(
                "/types/custom-types-test@2012-4-4.yang", false);
        YangStatementSourceImpl test1 = new YangStatementSourceImpl(
                "/context-test/test1.yang", false);

        StmtTestUtils.addSources(reactor, IETF);
        StmtTestUtils.addSources(reactor, types, test1);

        SchemaContext context = reactor.buildEffective();

        Module module = context.findModuleByName("test1",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-06-18"));
        assertNotNull(module);

//        String resource = "/ietf/ietf-inet-types@2010-09-24.yang";
//        InputStream stream = new FileInputStream(new File(getClass()
//                .getResource(resource).toURI()));
//        SchemaContext context = parser.resolveSchemaContext(TestUtils
//                .loadModules(Lists.newArrayList(stream)));
//        stream.close();
//
//        resource = "/context-test/test1.yang";
//        InputStream stream2 = new FileInputStream(new File(getClass()
//                .getResource(resource).toURI()));
//        Module module = TestUtils.loadModuleWithContext("test1", stream2,
//                context);
//        stream2.close();
//        assertNotNull(module);

        LeafSchemaNode leaf = (LeafSchemaNode) module.getDataChildByName("id");

        ExtendedType leafType = (ExtendedType) leaf.getType();
        QName qname = leafType.getQName();
        assertEquals(URI.create("urn:simple.demo.test1"), qname.getNamespace());
        assertEquals(simpleDateFormat.parse("2013-06-18"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        ExtendedType leafBaseType = (ExtendedType) leafType.getBaseType();
        qname = leafBaseType.getQName();
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:ietf-inet-types"),
                qname.getNamespace());
        assertEquals(simpleDateFormat.parse("2010-09-24"), qname.getRevision());
        assertEquals("port-number", qname.getLocalName());

        ExtendedType dscpExt = (ExtendedType) TestUtils.findTypedef(
                module.getTypeDefinitions(), "dscp-ext");
        List<RangeConstraint> ranges = dscpExt.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(BigInteger.ZERO, range.getMin());
        assertEquals(BigInteger.valueOf(63), range.getMax());
    }

    @Test
    public void testUsesFromContext() throws Exception {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        YangStatementSourceImpl test2 = new YangStatementSourceImpl(
                "/context-test/test2.yang", false);
        StmtTestUtils.addSources(reactor, BAZ, FOO, BAR, SUBFOO, test2);
        SchemaContext context = reactor.buildEffective();

        // Module testModule;
        // try (InputStream stream = new FileInputStream(new
        // File(getClass().getResource("/context-test/test2.yang")
        // .toURI()))) {
        // testModule = TestUtils.loadModuleWithContext("test2", stream,
        // context);
        // }
        // assertNotNull(testModule);

        // suffix _u = added by uses
        // suffix _g = defined in grouping from context

        // get grouping
        Module testModule = context.findModuleByName("test2",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-06-18"));
        assertNotNull(testModule);

        Module contextModule = context
                .findModuleByNamespace(URI.create("urn:opendaylight.baz"))
                .iterator().next();
        assertNotNull(contextModule);
        Set<GroupingDefinition> groupings = contextModule.getGroupings();
        assertEquals(1, groupings.size());
        GroupingDefinition grouping = groupings.iterator().next();

        // get node containing uses
        ContainerSchemaNode peer = (ContainerSchemaNode) testModule
                .getDataChildByName("peer");
        ContainerSchemaNode destination = (ContainerSchemaNode) peer
                .getDataChildByName("destination");

        // check uses
        Set<UsesNode> uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        AnyXmlSchemaNode data_u = (AnyXmlSchemaNode) destination
                .getDataChildByName("data");
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        AnyXmlSchemaNode data_g = (AnyXmlSchemaNode) grouping
                .getDataChildByName("data");
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));

        ChoiceSchemaNode how_u = (ChoiceSchemaNode) destination
                .getDataChildByName("how");
        assertNotNull(how_u);
        assertTrue(how_u.isAddedByUses());

        ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping
                .getDataChildByName("how");
        assertNotNull(how_g);
        assertFalse(how_g.isAddedByUses());
        assertFalse(how_u.equals(how_g));

        LeafSchemaNode address_u = (LeafSchemaNode) destination
                .getDataChildByName("address");
        assertNotNull(address_u);
        assertTrue(address_u.isAddedByUses());

        LeafSchemaNode address_g = (LeafSchemaNode) grouping
                .getDataChildByName("address");
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertFalse(address_u.equals(address_g));

        ContainerSchemaNode port_u = (ContainerSchemaNode) destination
                .getDataChildByName("port");
        assertNotNull(port_u);
        assertTrue(port_u.isAddedByUses());

        ContainerSchemaNode port_g = (ContainerSchemaNode) grouping
                .getDataChildByName("port");
        assertNotNull(port_g);
        assertFalse(port_g.isAddedByUses());
        assertFalse(port_u.equals(port_g));

        ListSchemaNode addresses_u = (ListSchemaNode) destination
                .getDataChildByName("addresses");
        assertNotNull(addresses_u);
        assertTrue(addresses_u.isAddedByUses());

        ListSchemaNode addresses_g = (ListSchemaNode) grouping
                .getDataChildByName("addresses");
        assertNotNull(addresses_g);
        assertFalse(addresses_g.isAddedByUses());
        assertFalse(addresses_u.equals(addresses_g));

        // grouping defined by 'uses'
        Set<GroupingDefinition> groupings_u = destination.getGroupings();
        assertEquals(1, groupings_u.size());
        GroupingDefinition grouping_u = groupings_u.iterator().next();
        assertTrue(grouping_u.isAddedByUses());

        // grouping defined in 'grouping' node
        Set<GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        GroupingDefinition grouping_g = groupings_g.iterator().next();
        assertFalse(grouping_g.isAddedByUses());
        assertFalse(grouping_u.equals(grouping_g));

        List<UnknownSchemaNode> nodes_u = destination.getUnknownSchemaNodes();
        assertEquals(1, nodes_u.size());
        UnknownSchemaNode node_u = nodes_u.get(0);
        assertTrue(node_u.isAddedByUses());

        List<UnknownSchemaNode> nodes_g = grouping.getUnknownSchemaNodes();
        assertEquals(1, nodes_g.size());
        UnknownSchemaNode node_g = nodes_g.get(0);
        assertFalse(node_g.isAddedByUses());
        assertFalse(node_u.equals(node_g));
    }

    @Test
    public void testUsesRefineFromContext() throws Exception {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        YangStatementSourceImpl test2 = new YangStatementSourceImpl(
                "/context-test/test2.yang", false);
        StmtTestUtils.addSources(reactor, BAZ, FOO, BAR, SUBFOO, test2);
        SchemaContext context = reactor.buildEffective();

        // Module module;
        // try (InputStream stream = new FileInputStream(new
        // File(getClass().getResource("/context-test/test2.yang")
        // .toURI()))) {
        // module = TestUtils.loadModuleWithContext("test2", stream, context);
        // }
        // assertNotNull(module);

        Module module = context.findModuleByName("test2",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-06-18"));
        assertNotNull(module);
        ContainerSchemaNode peer = (ContainerSchemaNode) module
                .getDataChildByName("peer");
        ContainerSchemaNode destination = (ContainerSchemaNode) peer
                .getDataChildByName("destination");
        Set<UsesNode> usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        UsesNode usesNode = usesNodes.iterator().next();

        // test grouping path
        List<QName> path = new ArrayList<>();
        QName qname = QName.create(URI.create("urn:opendaylight.baz"),
                simpleDateFormat.parse("2013-02-27"), "target");
        path.add(qname);
        SchemaPath expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, usesNode.getGroupingPath());

        // test refine
        Map<SchemaPath, SchemaNode> refines = usesNode.getRefines();
        assertEquals(3, refines.size());

        LeafSchemaNode refineLeaf = null;
        ContainerSchemaNode refineContainer = null;
        ListSchemaNode refineList = null;
        for (Map.Entry<SchemaPath, SchemaNode> entry : refines.entrySet()) {
            SchemaNode value = entry.getValue();
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
        assertEquals("description of address defined by refine",
                refineLeaf.getDescription());
        assertEquals("address reference added by refine",
                refineLeaf.getReference());
        assertFalse(refineLeaf.isConfiguration());
        assertTrue(refineLeaf.getConstraints().isMandatory());
        Set<MustDefinition> leafMustConstraints = refineLeaf.getConstraints()
                .getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        MustDefinition leafMust = leafMustConstraints.iterator().next();
        assertEquals(
                "ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)",
                leafMust.toString());

        // container port
        assertNotNull(refineContainer);
        Set<MustDefinition> mustConstraints = refineContainer.getConstraints()
                .getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals("description of port defined by refine",
                refineContainer.getDescription());
        assertEquals("port reference added by refine",
                refineContainer.getReference());
        assertFalse(refineContainer.isConfiguration());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        assertNotNull(refineList);
        assertEquals("description of addresses defined by refine",
                refineList.getDescription());
        assertEquals("addresses reference added by refine",
                refineList.getReference());
        assertFalse(refineList.isConfiguration());
        assertEquals(2, (int) refineList.getConstraints().getMinElements());
        assertEquals(12, (int) refineList.getConstraints().getMaxElements());
    }

    @Test
    public void testIdentity() throws Exception {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        YangStatementSourceImpl types = new YangStatementSourceImpl(
                "/types/custom-types-test@2012-4-4.yang", false);
        YangStatementSourceImpl test3 = new YangStatementSourceImpl(
                "/context-test/test3.yang", false);

        StmtTestUtils.addSources(reactor, IETF);
        StmtTestUtils.addSources(reactor, types, test3);
        SchemaContext context = reactor.buildEffective();

        Module module = context.findModuleByName("test3",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-06-18"));
        assertNotNull(module);

//        SchemaContext context;
//        File yangFile = new File(getClass().getResource(
//                "/types/custom-types-test@2012-4-4.yang").toURI());
//        File dependenciesDir = new File(getClass().getResource("/ietf").toURI());
//        YangContextParser parser = new YangParserImpl();
//        context = parser.parseFile(yangFile, dependenciesDir);
//
//        Module module;
//        try (InputStream stream = new FileInputStream(new File(getClass()
//                .getResource("/context-test/test3.yang").toURI()))) {
//            module = TestUtils.loadModuleWithContext("test3", stream, context);
//        }
//        assertNotNull(module);

        Set<IdentitySchemaNode> identities = module.getIdentities();
        assertEquals(1, identities.size());

        IdentitySchemaNode identity = identities.iterator().next();
        QName idQName = identity.getQName();
        assertEquals(URI.create("urn:simple.demo.test3"),
                idQName.getNamespace());
        assertEquals(simpleDateFormat.parse("2013-06-18"),
                idQName.getRevision());
        assertEquals("pt", idQName.getLocalName());

        IdentitySchemaNode baseIdentity = identity.getBaseIdentity();
        QName idBaseQName = baseIdentity.getQName();
        assertEquals(URI.create("urn:custom.types.demo"),
                idBaseQName.getNamespace());
        assertEquals(simpleDateFormat.parse("2012-04-16"),
                idBaseQName.getRevision());
        assertEquals("service-type", idBaseQName.getLocalName());
    }

    @Test
    public void testUnknownNodes() throws Exception {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        YangStatementSourceImpl types = new YangStatementSourceImpl(
                "/types/custom-types-test@2012-4-4.yang", false);
        YangStatementSourceImpl test3 = new YangStatementSourceImpl(
                "/context-test/test3.yang", false);

        StmtTestUtils.addSources(reactor, IETF);
        StmtTestUtils.addSources(reactor, types, test3);

        SchemaContext context = reactor.buildEffective();

        Module module = context.findModuleByName("test3",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-06-18"));
        assertNotNull(module);

//        SchemaContext context;
//        File yangFile = new File(getClass().getResource(
//                "/types/custom-types-test@2012-4-4.yang").toURI());
//        File dependenciesDir = new File(getClass().getResource("/ietf").toURI());
//        YangContextParser parser = new YangParserImpl();
//        context = parser.parseFile(yangFile, dependenciesDir);
//
//        Module module;
//        try (InputStream stream = new FileInputStream(new File(getClass()
//                .getResource("/context-test/test3.yang").toURI()))) {
//            module = TestUtils.loadModuleWithContext("test3", stream, context);
//        }

        ContainerSchemaNode network = (ContainerSchemaNode) module
                .getDataChildByName("network");
        List<UnknownSchemaNode> unknownNodes = network.getUnknownSchemaNodes();
        assertEquals(1, unknownNodes.size());

        UnknownSchemaNode un = unknownNodes.get(0);
        QName unType = un.getNodeType();
        assertEquals(URI.create("urn:custom.types.demo"), unType.getNamespace());
        assertEquals(simpleDateFormat.parse("2012-04-16"), unType.getRevision());
        assertEquals("mountpoint", unType.getLocalName());
        assertEquals("point", un.getNodeParameter());
        assertNotNull(un.getExtensionDefinition());
    }

    @Test
    public void testAugment() throws Exception {
        // load first module
        String resource = "/context-augment-test/test4.yang";
        SchemaContext context = parser.parseFiles(Collections
                .singleton(new File(getClass().getResource(resource).toURI())));

        // load another modules and parse them against already existing context
        File test1 = new File(getClass().getResource(
                "/context-augment-test/test1.yang").toURI());
        File test2 = new File(getClass().getResource(
                "/context-augment-test/test2.yang").toURI());
        File test3 = new File(getClass().getResource(
                "/context-augment-test/test3.yang").toURI());
        Set<Module> modules = parser.parseFiles(
                Arrays.asList(test1, test2, test3), context).getModules();
        assertNotNull(modules);

        Module t4 = TestUtils.findModule(modules, "test4");
        ContainerSchemaNode interfaces = (ContainerSchemaNode) t4
                .getDataChildByName("interfaces");
        ListSchemaNode ifEntry = (ListSchemaNode) interfaces
                .getDataChildByName("ifEntry");

        // test augmentation process
        ContainerSchemaNode augmentHolder = (ContainerSchemaNode) ifEntry
                .getDataChildByName("augment-holder");
        assertNotNull(augmentHolder);
        DataSchemaNode ds0 = augmentHolder
                .getDataChildByName("ds0ChannelNumber");
        assertNotNull(ds0);
        DataSchemaNode interfaceId = augmentHolder
                .getDataChildByName("interface-id");
        assertNotNull(interfaceId);
        DataSchemaNode higherLayerIf = augmentHolder
                .getDataChildByName("higher-layer-if");
        assertNotNull(higherLayerIf);
        ContainerSchemaNode schemas = (ContainerSchemaNode) augmentHolder
                .getDataChildByName("schemas");
        assertNotNull(schemas);
        assertNotNull(schemas.getDataChildByName("id"));

        // test augment target after augmentation: check if it is same instance
        ListSchemaNode ifEntryAfterAugment = (ListSchemaNode) interfaces
                .getDataChildByName("ifEntry");
        assertTrue(ifEntry == ifEntryAfterAugment);
    }

    @Test
    public void testDeviation() throws Exception {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        YangStatementSourceImpl bar = new YangStatementSourceImpl(
                "/model/bar.yang", false);
        YangStatementSourceImpl deviationTest = new YangStatementSourceImpl(
                "/context-test/deviation-test.yang", false);

        StmtTestUtils.addSources(reactor, bar, deviationTest);
        SchemaContext context = reactor.buildEffective();

        Module testModule = context.findModuleByName("deviation-test",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-02-27"));
        assertNotNull(testModule);

        // load first module
//        SchemaContext context;
//        String resource = "/model/bar.yang";
//
//        try (InputStream stream = new FileInputStream(new File(getClass()
//                .getResource(resource).toURI()))) {
//            context = parser.resolveSchemaContext(TestUtils.loadModules(Lists
//                    .newArrayList(stream)));
//        }

        // load another modules and parse them against already existing context
//        Set<Module> modules;
//        try (InputStream stream = new FileInputStream(new File(getClass()
//                .getResource("/context-test/deviation-test.yang").toURI()))) {
//            List<InputStream> input = Lists.newArrayList(stream);
//            modules = TestUtils.loadModulesWithContext(input, context);
//        }
//        assertNotNull(modules);

        // test deviation
        //Module testModule = TestUtils.findModule(modules, "deviation-test");


        Set<Deviation> deviations = testModule.getDeviations();
        assertEquals(1, deviations.size());
        Deviation dev = deviations.iterator().next();

        assertEquals("system/user ref", dev.getReference());

        URI expectedNS = URI.create("urn:opendaylight.bar");
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date expectedRev = simpleDateFormat.parse("2013-07-03");
        List<QName> path = new ArrayList<>();
        path.add(QName.create(expectedNS, expectedRev, "interfaces"));
        path.add(QName.create(expectedNS, expectedRev, "ifEntry"));
        SchemaPath expectedPath = SchemaPath.create(path, true);

        assertEquals(expectedPath, dev.getTargetPath());
        assertEquals(Deviate.ADD, dev.getDeviate());
    }

}
