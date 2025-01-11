/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.VersionInfo;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

@ExtendWith(MockitoExtension.class)
class YT1655Test extends AbstractTestModelTest {
    @Mock
    private VersionInfo version;

    @Test
    void testVersionInfoRead() throws Exception {
        final var tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION.copyBuilder()
            .setTrackVersionInfo(true)
            .build(), SCHEMA_CONTEXT);

        final var snap = tree.takeSnapshot();
        assertEquals(Optional.empty(), snap.readVersionInfo(YangInstanceIdentifier.of()));

        final var mod = snap.newModification();
        mod.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        mod.ready();

        tree.validate(mod);
        tree.commit(tree.prepare(mod), version);

        assertEquals(Optional.of(version), tree.takeSnapshot().readVersionInfo(YangInstanceIdentifier.of()));
    }
}
