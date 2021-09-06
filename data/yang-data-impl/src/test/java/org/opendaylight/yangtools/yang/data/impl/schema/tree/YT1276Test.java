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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1276Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName ONE = QName.create("one", "one");
    private static final QName TWO = QName.create("two", "two");
    private static final QName THREE = QName.create("three", "two");
    private static final QName FOZ = QName.create(FOO, "foz");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final QName BAZ = QName.create(FOO, "baz");
    private static final QName ONE_FOO = QName.create(ONE, "foo");
    private static final QName TWO_BAR = QName.create(TWO, "bar");
    private static final QName THREE_BAZ_DOUBLE_AUGMENT_MANDAT_LEAF =
        QName.create(THREE, "baz-mandatory");
    private static final QName THREE_BAZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF =
        QName.create(THREE, "baz-non-mandatory");
    private static final QName FOZ_AUGMENT = QName.create(FOO, "foz-augment");
    private static final QName FOZ_DOUBLE_AUGMENT_MANDAT_LEAF =
        QName.create(FOO, "foz-doubleaugment-mandatory");
    private static final QName FOZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF =
        QName.create(FOO, "foz-doubleaugment-non-mandatory");
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
        MODEL = YangParserTestUtils.parseYangResourceDirectory("/yt1276");
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
                        .withChild(ImmutableContainerNodeBuilder.create()
                            .withNodeIdentifier(new NodeIdentifier(XYZZY_AUGMENT_CONT))
                            .withChild(ImmutableContainerNodeBuilder.create()
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
    public void testWithAugmentedNestedBazWithoutMandatoryLeaf() throws DataValidationFailedException {
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
                    .withChild(ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(NodeIdentifier.create(XYZZY_AUGMENT_CONT))
                        .withChild(ImmutableContainerNodeBuilder.create()
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
                        .withChild(ImmutableContainerNodeBuilder.create()
                            .withNodeIdentifier(NodeIdentifier.create(XYZZY_AUGMENT_CONT))
                            .withChild(ImmutableContainerNodeBuilder.create()
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

    @Test
    public void testDoubleAugmentedFozWithMandatoryLeaf() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(FOZ), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOZ))
                .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(new AugmentationIdentifier(Set.of(FOZ_AUGMENT)))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(FOZ_AUGMENT))
                        .withChild(Builders.augmentationBuilder()
                            .withNodeIdentifier(new AugmentationIdentifier(Set.of(
                                FOZ_DOUBLE_AUGMENT_MANDAT_LEAF, FOZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF)))
                            .withChild(ImmutableNodes.leafNode(FOZ_DOUBLE_AUGMENT_MANDAT_LEAF, "abc"))
                            .build())
                        .build())
                    .build())
                .build());
        });
    }

    @Test
    public void testDoubleAugmentedFozWithoutMandatoryLeaf() {
        final IllegalArgumentException ex = assertFailsReady(mod -> {
            mod.write(YangInstanceIdentifier.of(FOZ), Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(FOZ))
                    .withChild(Builders.augmentationBuilder()
                            .withNodeIdentifier(new AugmentationIdentifier(Set.of(FOZ_AUGMENT)))
                            .withChild(Builders.containerBuilder()
                                    .withNodeIdentifier(new NodeIdentifier(FOZ_AUGMENT))
                                    .withChild(Builders.augmentationBuilder()
                                            .withNodeIdentifier(new AugmentationIdentifier(Set.of(
                                                FOZ_DOUBLE_AUGMENT_MANDAT_LEAF, FOZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF)))
                                            .withChild(ImmutableNodes.leafNode(
                                                FOZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF, "abc"))
                                            .build())
                                    .build())
                            .build())
                    .build());
        });
        assertEquals("Node (foo)foz is missing mandatory descendant "
            + "/AugmentationIdentifier{childNames=[(foo)foz-augment]}/(foo)foz-augment"
            + "/AugmentationIdentifier{childNames=[(foo)foz-doubleaugment-mandatory, "
            + "(foo)foz-doubleaugment-non-mandatory]}"
            + "/(foo)foz-doubleaugment-mandatory", ex.getMessage());
    }

    @Test
    public void testCrossModuleDoubleAugmentedBarWithMandatoryLeaf() throws DataValidationFailedException {
        applyOperation(mod -> {
            mod.write(YangInstanceIdentifier.of(ONE_FOO), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ONE_FOO))
                .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(new AugmentationIdentifier(Set.of(TWO_BAR)))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(TWO_BAR))
                        .withChild(Builders.augmentationBuilder()
                            .withNodeIdentifier(new AugmentationIdentifier(Set.of(
                                THREE_BAZ_DOUBLE_AUGMENT_MANDAT_LEAF, THREE_BAZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF)))
                            .withChild(ImmutableNodes.leafNode(THREE_BAZ_DOUBLE_AUGMENT_MANDAT_LEAF, "abc"))
                            .build())
                        .build())
                    .build())
                .build());
        });
    }

    @Test
    public void testCrossModuleDoubleAugmentedBarWithoutMandatoryLeaf() {
        final IllegalArgumentException ex = assertFailsReady(mod -> {
            mod.write(YangInstanceIdentifier.of(ONE_FOO), Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ONE_FOO))
                .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(new AugmentationIdentifier(Set.of(TWO_BAR)))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(TWO_BAR))
                        .withChild(Builders.augmentationBuilder()
                            .withNodeIdentifier(new AugmentationIdentifier(Set.of(
                                THREE_BAZ_DOUBLE_AUGMENT_MANDAT_LEAF, THREE_BAZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF)))
                            .withChild(ImmutableNodes.leafNode(
                                THREE_BAZ_DOUBLE_AUGMENT_NON_MANDAT_LEAF, "abc"))
                            .build())
                        .build())
                    .build())
                .build());
        });
        assertEquals("Node (one)foo is missing mandatory descendant "
            + "/AugmentationIdentifier{childNames=[(two)bar]}/(two)bar"
            + "/AugmentationIdentifier{childNames=[(three)baz-mandatory, (three)baz-non-mandatory]}"
            + "/(three)baz-mandatory", ex.getMessage());
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
