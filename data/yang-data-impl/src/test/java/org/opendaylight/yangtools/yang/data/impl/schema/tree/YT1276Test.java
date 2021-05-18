/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1276Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create(FOO, "bar");

    @Test
    public void testEnforce() throws DataValidationFailedException {
        final DataTree tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION,
            YangParserTestUtils.parseYangResource("/yt1276.yang"));

        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(FOO), Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.augmentationBuilder()
                .withNodeIdentifier(new AugmentationIdentifier(Set.of(BAR)))
                .withChild(ImmutableNodes.leafNode(BAR, "xyzzy"))
                .build())
            .build());
        mod.ready();
        tree.commit(tree.prepare(mod));
    }
}
