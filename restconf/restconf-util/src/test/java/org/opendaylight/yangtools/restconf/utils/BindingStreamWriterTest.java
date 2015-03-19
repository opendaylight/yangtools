/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.topology.unix.rev131222.network.topology.topology.node.path.computation.client.reported.lsp.lsp.tlvs.vs.tlv.vendor.payload.unix.UnixSubTlvs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.topology.unix.rev131222.network.topology.topology.node.path.computation.client.reported.lsp.lsp.tlvs.vs.tlv.vendor.payload.unix.UnixSubTlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.ReportedLsp1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.ReportedLsp1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.Lsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.LspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.Tlvs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.TlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.network.topology.topology.node.path.computation.client.reported.lsp.lsp.tlvs.vs.tlv.vendor.payload.LinuxBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.network.topology.topology.node.path.computation.client.reported.lsp.lsp.tlvs.vs.tlv.vendor.payload.linux.LinuxSubTlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.vs.tlv.VsTlv;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.vs.tlv.VsTlvBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.Node1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.PathComputationClient;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.PathComputationClientBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.path.computation.client.ReportedLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.path.computation.client.ReportedLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.path.computation.client.ReportedLspKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.DataObjectSerializerGenerator;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class BindingStreamWriterTest {

    private static final InstanceIdentifier<PathComputationClient> PATH_TO_CLIENT = InstanceIdentifier
            .builder(NetworkTopology.class).child(Topology.class, new TopologyKey(new TopologyId("foo"))).child(Node.class,new NodeKey(new NodeId("test"))).augmentation(Node1.class)
            .child(PathComputationClient.class).build();


    private static final InstanceIdentifier<Tlvs> PATH_TO_TLVS = PATH_TO_CLIENT.child(ReportedLsp.class, new ReportedLspKey("test")).augmentation(ReportedLsp1.class).child(Lsp.class).child(Tlvs.class);
    private static final InstanceIdentifier<UnixSubTlvs> PATH_TO_UNIX = PATH_TO_TLVS.child(VsTlv.class).child(UnixSubTlvs.class);

    private static final ReportedLspKey LSP1_KEY = new ReportedLspKey("one");
    private static final ReportedLspKey LSP2_KEY = new ReportedLspKey("two");


    private Optional<SchemaContext> schemaContext;
    private DataObjectSerializerGenerator generator;
    private BindingNormalizedNodeCodecRegistry registry;
    private BindingRuntimeContext runtimeContext;

    @Before
    public void setup() {
        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        schemaContext = moduleInfo.tryToCreateSchemaContext();
        JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        generator = StreamWriterGenerator.create(utils);
        registry = new BindingNormalizedNodeCodecRegistry(generator);
        runtimeContext = BindingRuntimeContext.create(moduleInfo, schemaContext.get());
        registry.onBindingRuntimeContextUpdated(runtimeContext);
    }



    @Test
    public void writeWithStreamAndBack() {
        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> result = registry.toNormalizedNode(PATH_TO_CLIENT, createTestData());
        NormalizedNode<?, ?> output = result.getValue();
        assertNotNull(output);
        assertTrue(output instanceof ContainerNode);
        Entry<InstanceIdentifier<?>, DataObject> deserialized = registry.fromNormalizedNode(result.getKey(), result.getValue());
        assertEquals(PATH_TO_CLIENT, deserialized.getKey());
        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> resultAfterDeserialize = registry.toNormalizedNode((InstanceIdentifier) PATH_TO_CLIENT, deserialized.getValue());
        assertEquals(result.getValue(), resultAfterDeserialize.getValue());

    }


    @Test
    public void writeWithStreamAPI() {
        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> result = registry.toNormalizedNode(PATH_TO_CLIENT, createTestData());
        NormalizedNode<?, ?> output = result.getValue();
        assertNotNull(output);
        assertTrue(output instanceof ContainerNode);

        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> unixResult = registry.toNormalizedNode(PATH_TO_UNIX, new UnixSubTlvsBuilder().setUnixValue((short) 10).build());
        assertNotNull(unixResult);
    }

    @Test
    public void testInstanceIdentifier() {
        YangInstanceIdentifier yang = registry.toYangInstanceIdentifier(PATH_TO_UNIX);
        assertNotNull(yang);
        InstanceIdentifier<?> binding = registry.fromYangInstanceIdentifier(yang);
        assertEquals(PATH_TO_UNIX,binding);
    }

    @Test
    @Ignore
    public void testPerformance() {
        for (int i = 1; i < 5; i++) {
            int repetitions = (int) Math.pow(10, i);
            measure("streamAPI: " + repetitions, repetitions, new Runnable() {
                @Override
                public void run() {
                    writeWithStreamAPI();
                }
            });
        }
    }

    private void measure(final String name, final int repetitions, final Runnable runnable) {
        runnable.run(); // WARM UP
        long start = System.nanoTime();
        // TODO Auto-generated method stub
        for (int i = 0; i < repetitions; i++) {
            runnable.run();
        }
        long finish = System.nanoTime();
        // To Miliseconds
        System.out.println(String.format("Type: %s Time: %f", name, (finish - start) / 1000000.d));
    }

    PathComputationClient createTestData() {
        return new PathComputationClientBuilder().setReportedLsp(
                ImmutableList.<ReportedLsp> builder().add(reportedLsp(LSP1_KEY)).add(reportedLsp(LSP2_KEY)).build())
                .build();
    }

    private ReportedLsp reportedLsp(final ReportedLspKey lspKey) {
        return new ReportedLspBuilder()
                .setKey(lspKey)
                .addAugmentation(
                        ReportedLsp1.class,
                        new ReportedLsp1Builder().setLsp(
                                new LspBuilder().setTlvs(
                                        new TlvsBuilder().setVsTlv(
                                                new VsTlvBuilder().setVendorPayload(
                                                        new LinuxBuilder().setLinuxSubTlvs(
                                                                new LinuxSubTlvsBuilder().setLinuxValue((short) 50)
                                                                        .build()).build()).build()).build()).build())
                                .build()).build();
    }

}
