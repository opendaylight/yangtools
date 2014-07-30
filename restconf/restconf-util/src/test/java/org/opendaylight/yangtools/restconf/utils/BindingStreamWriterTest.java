/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.ReportedLsp1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.ReportedLsp1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.LspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.TlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.network.topology.topology.node.path.computation.client.reported.lsp.lsp.tlvs.vs.tlv.vendor.payload.LinuxBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.network.topology.topology.node.path.computation.client.reported.lsp.lsp.tlvs.vs.tlv.vendor.payload.linux.LinuxSubTlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.vs.tlv.VsTlvBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.Node1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.PathComputationClient;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.PathComputationClientBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.path.computation.client.ReportedLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.path.computation.client.ReportedLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.pcep.client.attributes.path.computation.client.ReportedLspKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingSchemaContextUtils;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.sal.binding.generator.stream.api.StaticCodecBinder;
import org.opendaylight.yangtools.sal.binding.generator.stream.impl.GeneratedStreamEmitterRegistry;
import org.opendaylight.yangtools.sal.binding.generator.stream.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class BindingStreamWriterTest {

    private static final InstanceIdentifier<PathComputationClient> PATH_TO_CLIENT = InstanceIdentifier
            .builder(NetworkTopology.class).child(Topology.class).child(Node.class).augmentation(Node1.class)
            .child(PathComputationClient.class).build();
    private static final ReportedLspKey LSP1_KEY = new ReportedLspKey("one");
    private static final ReportedLspKey LSP2_KEY = new ReportedLspKey("two");

    private RuntimeGeneratedMappingServiceImpl mappingService;
    private Optional<SchemaContext> schemaContext;
    private StreamWriterGenerator generator;
    private GeneratedStreamEmitterRegistry registry;
    private DataSchemaNode schema;

    @Before
    public void setup() {
        this.mappingService = new RuntimeGeneratedMappingServiceImpl(ClassPool.getDefault());

        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        schemaContext = moduleInfo.tryToCreateSchemaContext();
        this.mappingService.onGlobalContextUpdated(moduleInfo.tryToCreateSchemaContext().get());
        BindingGeneratorImpl genImpl = new BindingGeneratorImpl();
        genImpl.generateTypes(schemaContext.get());

        generator = new StreamWriterGenerator(ClassPool.getDefault(),
                (StaticCodecBinder) mappingService.getCodecRegistry(), mappingService.getCodecRegistry());
        Map<Module, ModuleContext> moduleCtxs = genImpl.getModuleContexts();
        for (Entry<Module, ModuleContext> entry : moduleCtxs.entrySet()) {
            generator.onModuleContextAdded(entry.getKey(), entry.getValue());
        }
        registry = new GeneratedStreamEmitterRegistry(generator, mappingService.getCodecRegistry());
        registry.onGlobalContextUpdated(schemaContext.get());
        schema = (DataSchemaNode) BindingSchemaContextUtils.findDataNodeContainer(schemaContext.get(), PATH_TO_CLIENT)
                .get();
    }

    @Test
    public void writeWithStreamAPI() {

        DataObjectSerializer emitter = registry.getSerializer(PathComputationClient.class);
        assertNotNull(emitter);
        NormalizedNodeResult builder = new NormalizedNodeResult();
        NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(builder);
        emitter.serialize(createTestData(), registry.newWriter(PATH_TO_CLIENT, domWriter));
        NormalizedNode<?, ?> output = builder.getResult();
        assertNotNull(output);
        assertTrue(output instanceof ContainerNode);
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
