package org.opendaylight.yangtools.it.yang.runtime.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.DataObjectReadingUtil;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class BindingReadingTest {

    private static final TopologyId TOPOLOGY_FOO_ID = new TopologyId("foo:topo");
    private static final TopologyId TOPOLOGY_BAR_ID = new TopologyId("bar:topo");
    private static final TopologyId TOPOLOGY_BAZ_ID = new TopologyId("baz:topo");
    private static final LinkId LINK_BAR_ID = new LinkId("bar:link:1");
    private static final NodeId SOURCE_NODE_ID = new NodeId("node:id");
    private static final TpId SOURCE_TP_ID = new TpId("source:tp");
    private static final InstanceIdentifier<NetworkTopology> NETWORK_TOPOLOGY_PATH = InstanceIdentifier.builder(
            NetworkTopology.class).build();
    private NetworkTopology networkModel;
    private Link linkModel;

    private static final InstanceIdentifier<Topology> TOPOLOGY_BAR_PATH = NETWORK_TOPOLOGY_PATH //
            .child(Topology.class, new TopologyKey(TOPOLOGY_BAR_ID));

    private static final InstanceIdentifier<Link> LINK_BAR_PATH = NETWORK_TOPOLOGY_PATH.builder() //
            .child(Topology.class, new TopologyKey(TOPOLOGY_BAR_ID)) //
            .child(Link.class, new LinkKey(LINK_BAR_ID)) //
            .build();

    private static final InstanceIdentifier<Link> WILDCARDED_LINK_PATH = NETWORK_TOPOLOGY_PATH.builder() //
            .child(Topology.class) //
            .child(Link.class) //
            .build();

    private static final InstanceIdentifier<Source> ABSOLUTE_SOURCE_PATH = TOPOLOGY_BAR_PATH.builder() //
            .child(Link.class, new LinkKey(LINK_BAR_ID)) //
            .child(Source.class) //
            .build();

    private static final InstanceIdentifier<Source> WILDCARDED_SOURCE_PATH = NETWORK_TOPOLOGY_PATH.builder() //
            .child(Topology.class) //
            .child(Link.class, new LinkKey(LINK_BAR_ID)) //
            .child(Source.class) //
            .build();

    /**
     *
     * Creates network topology model with three topologies:
     * foo,bar and baz.
     * Where bar has 1 link, and baz has 2 links.
     *
     */
    @Before
    public void createTopology() {
        linkModel = new LinkBuilder() //
                .setLinkId(LINK_BAR_ID) //
                .setSource(new SourceBuilder() //
                        .setSourceNode(SOURCE_NODE_ID) //
                        .setSourceTp(SOURCE_TP_ID) //
                        .build()) //
                .build();
        networkModel = new NetworkTopologyBuilder().setTopology(ImmutableList.<Topology> builder() //
                .add(new TopologyBuilder() //
                        .setTopologyId(TOPOLOGY_FOO_ID) //
                        .setServerProvided(true) //
                        .build()) //
                .add(new TopologyBuilder() //
                        .setTopologyId(TOPOLOGY_BAR_ID) //
                        .setServerProvided(false) //
                        .setLink(ImmutableList.<Link> builder() //
                                .add(linkModel) //
                                .build()) //
                        .build()) //
                .add(new TopologyBuilder() //
                        .build())//
                .add(new TopologyBuilder() //
                        .setTopologyId(TOPOLOGY_BAZ_ID)//
                        .setLink(ImmutableList.<Link> builder() //
                                .add(new LinkBuilder().setLinkId(new LinkId("link:2")).build()) //
                                .add(new LinkBuilder().setLinkId(new LinkId("link:3")).build()) //
                                .build()) //
                        .build()) //
                .build()) //
                .build(); //
    }

    @Test
    public void testContainerRead() {
        Optional<Source> source = DataObjectReadingUtil.readData(linkModel, Source.class);
        assertNotNull(source);
        assertEquals(linkModel.getSource(), source.get());
    }

    @Test
    public void testInstanceIdentifierRead() {
        Map<InstanceIdentifier<Source>, Source> source = DataObjectReadingUtil.readData(networkModel,
                NETWORK_TOPOLOGY_PATH, ABSOLUTE_SOURCE_PATH);
        assertNotNull(source);
        Source potentialSource = source.get(ABSOLUTE_SOURCE_PATH);
        assertEquals(linkModel.getSource(), potentialSource);
    }

    @Test
    public void testInstanceIdentifierReadWildcarded() {
        Topology topology = DataObjectReadingUtil.readData(networkModel, NETWORK_TOPOLOGY_PATH, TOPOLOGY_BAR_PATH).get(
                TOPOLOGY_BAR_PATH);
        Map<InstanceIdentifier<Source>, Source> source = DataObjectReadingUtil.readData(topology, TOPOLOGY_BAR_PATH,
                WILDCARDED_SOURCE_PATH);
        assertNotNull(source);
        Source potentialSource = source.get(ABSOLUTE_SOURCE_PATH);
        assertEquals(linkModel.getSource(), potentialSource);
    }

    @Test
    public void testInstanceIdentifierReadNonExistingValue() {
        InstanceIdentifier<Source> sourcePath = NETWORK_TOPOLOGY_PATH.builder() //
                .child(Topology.class, new TopologyKey(TOPOLOGY_BAZ_ID)) //
                .child(Link.class, new LinkKey(LINK_BAR_ID)) //
                .child(Source.class) //
                .build();
        Map<InstanceIdentifier<Source>, Source> source = DataObjectReadingUtil.readData(networkModel,
                NETWORK_TOPOLOGY_PATH, sourcePath);
        assertNotNull(source);
        assertTrue(source.isEmpty());
    }

    @Test
    public void testWildcardedListRead() {
        Topology topology = DataObjectReadingUtil.readData(networkModel, NETWORK_TOPOLOGY_PATH, TOPOLOGY_BAR_PATH).get(TOPOLOGY_BAR_PATH);

        Map<InstanceIdentifier<Link>, Link> potentialLinks = DataObjectReadingUtil.readData(topology, TOPOLOGY_BAR_PATH, WILDCARDED_LINK_PATH);
        assertFalse(potentialLinks.isEmpty());
        assertEquals(1, potentialLinks.size());
        assertEquals(linkModel, potentialLinks.get(LINK_BAR_PATH));
    }

    @Test
    public void testTwoWildcardsListRead() {

        Map<InstanceIdentifier<Link>, Link> potentialLinks = DataObjectReadingUtil.readData(networkModel, NETWORK_TOPOLOGY_PATH, WILDCARDED_LINK_PATH);
        assertFalse(potentialLinks.isEmpty());
        assertEquals(3, potentialLinks.size());
        assertEquals(linkModel, potentialLinks.get(LINK_BAR_PATH));
        HashSet<Link> allLinks = new HashSet<>(potentialLinks.values());
        assertEquals(3, allLinks.size());
        for(InstanceIdentifier<Link> key : potentialLinks.keySet()) {
            assertFalse("Returned instance identifier must not be wildcarded.", key.isWildcarded());
        }

    }

}
