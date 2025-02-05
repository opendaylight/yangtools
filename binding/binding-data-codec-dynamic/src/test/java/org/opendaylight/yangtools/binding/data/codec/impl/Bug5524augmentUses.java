/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.ListModule11Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.list.module1._1.ListModule12Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module1.rev160101.grouping.module1.list.module1._1.list.module1._2.ContainerModule1Builder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module3.rev160101.grouping.module3.ContainerManualListModule11Builder;
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
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.bug._5524.module4.rev160101.module4.main.container.module._4.ManualContainerModule11Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class Bug5524augmentUses extends AbstractBindingCodecTest {
    @Test
    void testBug5224() {
        final var module4Main = new Module4MainBuilder()
            .setContainerModule4(new ContainerModule4Builder()
                .setListModule11(List.of(new ListModule11Builder()
                    .setListModule12(List.of(new ListModule12Builder()
                        .setContainerModule1(new ContainerModule1Builder()
                            .addAugmentation(new ContainerModule11Builder().build())
                            .build())
                        .build()))
                    .build()))
                .build())
            .build();

        final var manualModule4Main = new Module4MainBuilder()
            .setContainerModule4(new ContainerModule4Builder()
                .setManualListModule11(List.of(new ManualListModule11Builder()
                    .setManualListModule12(List.of(new ManualListModule12Builder()
                        .setManualContainerModule1(new ManualContainerModule1Builder()
                            .addAugmentation(new ManualContainerModule11Builder().build())
                            .build())
                        .build()))
                    .build()))
                .build())
            .build();

        final var contManualModule4Main = new Module4MainBuilder()
            .setContainerModule4(new ContainerModule4Builder()
                .setContainerManualListModule11(List.of(new ContainerManualListModule11Builder()
                    .setContainerManualListModule12(List.of(new ContainerManualListModule12Builder()
                        .setContainerManualContainerModule1(new ContainerManualContainerModule1Builder()
                            .setContainerManualContainerModule2(new ContainerManualContainerModule2Builder().build())
                            .build())
                        .build()))
                    .build()))
                .build())
            .build();

        final var subtreeCodec = codecContext.getDataObjectCodec(InstanceIdentifier.create(Module4Main.class));
        final var serialized = subtreeCodec.serialize(module4Main);
        final var manualSerialized = subtreeCodec.serialize(manualModule4Main);
        final var containerManualSerialized = subtreeCodec.serialize(contManualModule4Main);
        assertNotNull(serialized);
        assertNotNull(manualSerialized);
        assertNotNull(containerManualSerialized);
    }
}
