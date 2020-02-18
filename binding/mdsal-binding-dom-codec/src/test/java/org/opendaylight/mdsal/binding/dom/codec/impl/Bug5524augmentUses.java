/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.ListModule11Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.list.module1._1.ListModule12Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.list.module1._1.list.module1._2.ContainerModule1Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.ContainerManualListModule11Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.ContainerModule11;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.ContainerModule11Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.ManualListModule11Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.container.manual.list.module1._1.ContainerManualListModule12Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.container.manual.list.module1._1.container.manual.list.module1._2.ContainerManualContainerModule1Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.container.manual.list.module1._1.container.manual.list.module1._2.container.manual.container.module1.ContainerManualContainerModule2Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.manual.list.module1._1.ManualListModule12Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.manual.list.module1._1.manual.list.module1._2.ManualContainerModule1Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.Module4Main;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.Module4MainBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.module4.main.ContainerModule4Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.module4.main.container.module._4.ManualContainerModule11;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.module4.main.container.module._4.ManualContainerModule11Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class Bug5524augmentUses extends AbstractBindingCodecTest {
    @Test
    public void testBug5224() throws Exception {
        final Module4Main module4Main = new Module4MainBuilder().setContainerModule4(
                new ContainerModule4Builder().setListModule11(Collections.singletonList(
                        new ListModule11Builder().setListModule12(Collections.singletonList(
                                new ListModule12Builder().setContainerModule1(
                                        new ContainerModule1Builder().addAugmentation(ContainerModule11.class,
                                                new ContainerModule11Builder().build()).build())
                                                    .build())).build())).build()).build();

        final Module4Main manualModule4Main = new Module4MainBuilder().setContainerModule4(
                new ContainerModule4Builder().setManualListModule11(Collections.singletonList(
                        new ManualListModule11Builder().setManualListModule12(Collections.singletonList(
                                new ManualListModule12Builder().setManualContainerModule1(
                                        new ManualContainerModule1Builder().addAugmentation(
                                                ManualContainerModule11.class, new ManualContainerModule11Builder()
                                                    .build()).build()).build())).build())).build()).build();

        final Module4Main contManualModule4Main = new Module4MainBuilder().setContainerModule4(
                new ContainerModule4Builder().setContainerManualListModule11(Collections.singletonList(
                        new ContainerManualListModule11Builder().setContainerManualListModule12(
                                Collections.singletonList(
                                new ContainerManualListModule12Builder().setContainerManualContainerModule1(
                                        new ContainerManualContainerModule1Builder().setContainerManualContainerModule2(
                                                new ContainerManualContainerModule2Builder().build()).build())
                                        .build())).build())).build()).build();

        final BindingCodecTree codecContext = registry.getCodecContext();
        final BindingDataObjectCodecTreeNode<Module4Main> subtreeCodec = codecContext.getSubtreeCodec(
                InstanceIdentifier.create(Module4Main.class));
        final NormalizedNode<?, ?> serialized = subtreeCodec.serialize(module4Main);
        final NormalizedNode<?, ?> manualSerialized = subtreeCodec.serialize(manualModule4Main);
        final NormalizedNode<?, ?> containerManualSerialized = subtreeCodec.serialize(contManualModule4Main);
        assertNotNull(serialized);
        assertNotNull(manualSerialized);
        assertNotNull(containerManualSerialized);
    }
}
