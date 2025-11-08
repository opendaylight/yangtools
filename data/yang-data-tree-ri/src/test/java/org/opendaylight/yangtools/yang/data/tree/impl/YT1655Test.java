/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

@ExtendWith(MockitoExtension.class)
class YT1655Test extends AbstractTestModelTest {
    @Mock
    private VersionInfo version1;
    @Mock
    private VersionInfo version2;

    @Test
    void testVersionInfoRead() throws Exception {
        final var tree = new ReferenceDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION.toBuilder()
            .setTrackVersionInfo(true)
            .build(), MODEL_CONTEXT);

        final var snap = tree.takeSnapshot();
        assertEquals(Optional.empty(), snap.readVersionInfo(YangInstanceIdentifier.of()));

        final var mod = snap.newModification();
        mod.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        mod.ready();

        tree.validate(mod);
        tree.commit(tree.prepare(mod), version1);

        final var snap2 = tree.takeSnapshot();
        assertEquals(Optional.of(version1), snap2.readVersionInfo(YangInstanceIdentifier.of()));
        assertEquals(Optional.of(version1), snap2.readVersionInfo(TestModel.TEST_PATH));

        // noop
        final var mod2 = snap2.newModification();
        assertEquals(Optional.of(version1), mod2.readVersionInfo(YangInstanceIdentifier.of()));
        mod2.ready();
        assertEquals(Optional.of(version1), mod2.readVersionInfo(YangInstanceIdentifier.of()));

        // second modification
        final var mod3 = snap2.newModification();
        mod3.write(TestModel.NAME_PATH, ImmutableNodes.leafNode(TestModel.NAME_QNAME, "foo"));
        mod3.ready();

        // mod3.version does not have info yet
        // FIXME: this seems odd, though
        assertEquals(Optional.empty(), mod3.readVersionInfo(YangInstanceIdentifier.of()));
        assertNotNull(mod3.newModification());
        assertEquals(Optional.empty(), mod3.readVersionInfo(YangInstanceIdentifier.of()));

        tree.validate(mod3);
        tree.commit(tree.prepare(mod3), version2);

        assertEquals(Optional.of(version2), mod3.readVersionInfo(YangInstanceIdentifier.of()));
        assertEquals(Optional.of(version2), mod3.readVersionInfo(TestModel.NAME_PATH));
        assertEquals(Optional.of(version1), mod3.readVersionInfo(TestModel.TEST_PATH));
    }
}
