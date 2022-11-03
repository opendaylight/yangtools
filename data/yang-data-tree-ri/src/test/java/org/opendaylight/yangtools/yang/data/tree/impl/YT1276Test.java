/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

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
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1276Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final QName BAZ = QName.create(FOO, "baz");
    private static final QName XYZZY_LEAF = QName.create(FOO, "xyzzy-leaf");
    private static final QName XYZZY_AUGMENT = QName.create(FOO, "xyzzy-augment");
    private static final QName XYZZY_AUGMENT_CONT = QName.create(FOO, "xyzzy-augment-container");
    private static final QName XYZZY_AUGMENT_CONT_INNER = QName.create(FOO, "xyzzy-augment-container-inner");
    private static final QName XYZZY_AUGMENT_CONT_LEAF = QName.create(FOO, "xyzzy-augment-container-leaf");
    private static final QName BAZ_AUG_CASE_MANDAT_LEAF = QName.create(FOO, "augmented-case-mandatory");
    private static final QName BAZ_AUG_CASE_NON_MANDAT_LEAF = QName.create(FOO, "augmented-case-non-mandatory");
    private static final QName NESTED_BAZ_CHOICE = QName.create(FOO, "nested-baz");
    private static final QName NESTED_BAZ_XYZ_CASE_MANDATORY = QName.create(FOO, "nested-xyz-mandatory");
    private static final QName NESTED_BAZ_XYZ_CASE_NON_MANDATORY = QName.create(FOO, "nested-xyz-non-mandatory");

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
    public void testBarWithXyzzyWithSubtree() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(ImmutableNodes.leafNode(XYZZY_LEAF, "xyzzy"))
                    .withChild(Builders.augmentationBuilder()
                        .withNodeIdentifier(new AugmentationIdentifier(Set.of(XYZZY_AUGMENT, XYZZY_AUGMENT_CONT)))
                        .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT, "xyzzy"))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(new NodeIdentifier(XYZZY_AUGMENT_CONT))
                            .withChild(Builders.containerBuilder()
                                .withNodeIdentifier(new NodeIdentifier(XYZZY_AUGMENT_CONT_INNER))
                                .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT_CONT_LEAF, "aug-cont-leaf"))
                                .build())
                            .build())
                        .build())
                    .build())
                .build());
        });
    }

    @Test
    public void testBazWithAugmentedCaseWithMandatoryLeaf() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(ImmutableNodes.leafNode(BAZ_AUG_CASE_MANDAT_LEAF, "augmentedCaseMandatory"))
                    .withChild(ImmutableNodes.leafNode(BAZ_AUG_CASE_NON_MANDAT_LEAF, "augmentedCaseNonMandatory"))
                    .build())
                .build());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBazWithAugmentedCaseWithoutMandatoryLeaf() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(ImmutableNodes.leafNode(BAZ_AUG_CASE_NON_MANDAT_LEAF, "augmentedCaseNonMandatory"))
                    .build())
                .build());
        });
    }

    @Test
    public void testWithAugmentedNestedBazWithMandatoryLeaf() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(Builders.choiceBuilder()
                        .withNodeIdentifier(new NodeIdentifier(NESTED_BAZ_CHOICE))
                        .withChild(ImmutableNodes.leafNode(NESTED_BAZ_XYZ_CASE_MANDATORY, "nestedMandatory"))
                        .withChild(ImmutableNodes.leafNode(NESTED_BAZ_XYZ_CASE_NON_MANDATORY, "nestedNonMandatory"))
                    .build())
                    .build())
                .build());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithAugmentedNestedBazWithhoutMandatoryLeaf() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(BAR), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAZ))
                    .withChild(Builders.choiceBuilder()
                        .withNodeIdentifier(new NodeIdentifier(NESTED_BAZ_CHOICE))
                        .withChild(ImmutableNodes.leafNode(NESTED_BAZ_XYZ_CASE_NON_MANDATORY, "nestedNonMandatory"))
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
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(XYZZY_AUGMENT_CONT))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(XYZZY_AUGMENT_CONT_INNER))
                            .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT_CONT_LEAF, "aug-cont-leaf"))
                            .build())
                        .build())
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
                        .withNodeIdentifier(new AugmentationIdentifier(Set.of(XYZZY_AUGMENT, XYZZY_AUGMENT_CONT)))
                        .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT, "xyzzy"))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(XYZZY_AUGMENT_CONT))
                            .withChild(Builders.containerBuilder()
                                .withNodeIdentifier(NodeIdentifier.create(XYZZY_AUGMENT_CONT_INNER))
                                .withChild(ImmutableNodes.leafNode(XYZZY_AUGMENT_CONT_LEAF, "aug-cont-leaf"))
                                .build())
                            .build())
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
        assertEquals("Node (foo)baz is missing mandatory descendant /AugmentationIdentifier{childNames="
                + "[(foo)xyzzy-augment, (foo)xyzzy-augment-container]}/(foo)xyzzy-augment", ex.getMessage());

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
