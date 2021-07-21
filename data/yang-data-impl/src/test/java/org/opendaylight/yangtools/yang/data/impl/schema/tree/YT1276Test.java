/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Set;
import java.util.function.Consumer;
import org.junit.BeforeClass;
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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1276Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final QName BAZ = QName.create(FOO, "baz");
    private static final QName XYZZY_LEAF = QName.create(FOO, "xyzzy-leaf");
    private static final QName XYZZY_AUGMENT = QName.create(FOO, "xyzzy-augment");

    private static EffectiveModelContext MODEL;

    private final DataTree tree = new InMemoryDataTreeFactory()
        .create(DataTreeConfiguration.DEFAULT_CONFIGURATION, MODEL);

    @BeforeClass
    public static void beforeClass() {
        MODEL = YangParserTestUtils.parseYangResource("/yt1276.yang");
    }

    @Test
    public void testFooWithBar() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(FOO), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(new AugmentationIdentifier(Set.of(BAR)))
                    .withChild(ImmutableNodes.leafNode(BAR, "xyzzy"))
                    .build())
                .build());
        });
    }

    @Test
    @Deprecated
    public void testFooWithBarLegacy() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(FOO), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(ImmutableNodes.leafNode(BAR, "xyzzy"))
                .build());
        });
    }

    @Test
    public void testFooWithoutBar() {
        final IllegalArgumentException ex = assertFailsReady(mod -> {
            mod.write(YangInstanceIdentifier.of(FOO), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .build());
        });
        assertEquals(
            "Node (foo)foo is missing mandatory descendant /AugmentationIdentifier{childNames=[(foo)bar]}/(foo)bar",
            ex.getMessage());
    }

    @Test
    public void testBarWithXyzzy() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(ImmutableNodes.leafNode(XYZZY_LEAF, "xyzzy"))
                    .withChild(Builders.augmentationBuilder()
                        .withNodeIdentifier(new AugmentationIdentifier(Set.of(XYZZY_AUGMENT)))
                        .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT, "xyzzy"))
                        .build())
                    .build())
                .build());
        });
    }

    @Test
    @Deprecated
    public void testBarWithXyzzyLegacy() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(ImmutableNodes.leafNode(XYZZY_LEAF, "xyzzy"))
                    .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT, "xyzzy"))
                    .build())
                .build());
        });
    }

    @Test
    public void testBarWithoutXyzzyLeaf() {
        final IllegalArgumentException ex = assertFailsReady(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(Builders.augmentationBuilder()
                        .withNodeIdentifier(new AugmentationIdentifier(Set.of(XYZZY_AUGMENT)))
                        .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT, "xyzzy"))
                        .build())
                    .build())
                .build());
        });
        assertEquals(
            "Node (foo)baz is missing mandatory descendant /(foo)xyzzy-leaf",
            ex.getMessage());
    }

    @Test
    public void testBarWithoutXyzzyAugment() {
        final IllegalArgumentException ex = assertFailsReady(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(ImmutableNodes.leafNode(XYZZY_LEAF, "xyzzy"))
                    .build())
                .build());
        });
        assertEquals("Node (foo)baz is missing mandatory descendant "
            + "/AugmentationIdentifier{childNames=[(foo)xyzzy-augment]}/(foo)xyzzy-augment",
            ex.getMessage());

    }

    private IllegalArgumentException assertFailsReady(final Consumer<DataTreeModification> operation) {
        return assertThrows(IllegalArgumentException.class, () -> applyOperation(operation));
    }

    private void applyOperation(final Consumer<DataTreeModification> operation)
            throws DataValidationFailedException {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        operation.accept(mod);
        mod.ready();
        tree.commit(tree.prepare(mod));
    }
}
