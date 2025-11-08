/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug4295Test {
    private static final QName ROOT = QName.create("foo", "root");
    private static final QName SUB_ROOT = QName.create(ROOT, "sub-root");
    private static final QName OUTER_LIST = QName.create(ROOT, "outer-list");
    private static final QName INNER_LIST = QName.create(ROOT, "inner-list");
    private static final QName O_ID = QName.create(ROOT, "o-id");
    private static final QName I_ID = QName.create(ROOT, "i-id");
    private static final QName O = QName.create(ROOT, "o");
    private static final QName I = QName.create(ROOT, "i");

    private final DataTree tree = new ReferenceDataTreeFactory().create(
        DataTreeConfiguration.DEFAULT_OPERATIONAL, YangParserTestUtils.parseYang("""
            module foo {
              namespace "foo";
              prefix foo;

              container root {
                container sub-root {
                  list outer-list {
                    ordered-by user;
                    key "o-id";
                    leaf o-id {
                      type string;
                    }
                    list inner-list {
                      ordered-by user;
                      key "i-id";
                      leaf i-id {
                        type string;
                      }
                      leaf i {
                        type string;
                      }
                    }
                    leaf o {
                      type string;
                    }
                  }
                }
              }
            }"""));

    @Test
    void test() throws Exception {
        // MERGE
        final var mod = tree.takeSnapshot().newModification();
        mod.merge(YangInstanceIdentifier.of(ROOT), createRootContainerBuilder()
            .withChild(createSubRootContainerBuilder()
                .withChild(ImmutableNodes.newUserMapBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(OUTER_LIST))
                    .withChild(createOuterListEntry("1", "o-1"))
                    .withChild(createOuterListEntry("2", "o-2"))
                    .withChild(createOuterListEntry("3", "o-3"))
                    .build())
                .build())
            .build());

        // WRITE INNER LIST WITH ENTRIES
        mod.write(YangInstanceIdentifier.of(ROOT).node(SUB_ROOT).node(OUTER_LIST).node(createOuterListEntryPath("2"))
            .node(INNER_LIST), createInnerListBuilder()
            .withChild(createInnerListEntry("a", "i-a"))
            .withChild(createInnerListEntry("b", "i-b"))
            .build());

        // COMMIT
        mod.ready();
        tree.validate(mod);
        final var candidate = tree.prepare(mod);
        tree.commit(candidate);

        final var root = candidate.getRootNode();
        assertEquals(ModificationType.SUBTREE_MODIFIED, root.modificationType());

        final var rootIt = root.childNodes().iterator();
        assertWrite(rootIt.next(), new NodeIdentifier(ROOT));
        assertFalse(rootIt.hasNext());

        assertCandidateOne(secondModification(1));
        assertCandidateTwo(secondModification(2));
        assertCandidateThree(secondModification(3));
    }

    private static void assertCandidateOne(final DataTreeCandidateNode top) {
        assertEquals(ModificationType.SUBTREE_MODIFIED, top.modificationType());
        final var topIt = top.childNodes().iterator();
        final var rootIt = assertSubtreeModified(topIt.next(), new NodeIdentifier(ROOT), 1);
        assertFalse(topIt.hasNext());

        final var subRootIt = assertSubtreeModified(rootIt.next(), new NodeIdentifier(SUB_ROOT), 1);
        assertFalse(rootIt.hasNext());

        final var outerListIt = assertSubtreeModified(subRootIt.next(), new NodeIdentifier(OUTER_LIST), 4);
        assertFalse(subRootIt.hasNext());
        final var outerListFirstIt = assertSubtreeModified(outerListIt.next(),
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "2"), 1);
        assertDelete(outerListFirstIt.next(), new NodeIdentifier(INNER_LIST));
        assertFalse(outerListFirstIt.hasNext());

        assertWrite(outerListIt.next(), NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "5"));
        assertWrite(outerListIt.next(), NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "4"));

        final var outerListFourth = outerListIt.next();
        final var outerListFourthIt = assertSubtreeModified(outerListFourth,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "3"), 2);
        assertFalse(outerListIt.hasNext());

        // O and O_ID, in some order
        assertOuterEntry(outerListFourthIt, outerListFourth, "o-3", "3");
        assertFalse(outerListFourthIt.hasNext());
    }

    private static void assertCandidateTwo(final DataTreeCandidateNode top) {
        assertEquals(ModificationType.SUBTREE_MODIFIED, top.modificationType());
        final var topIt = top.childNodes().iterator();
        final var rootIt = assertSubtreeModified(topIt.next(), new NodeIdentifier(ROOT), 1);
        assertFalse(topIt.hasNext());

        final var subRootIt = assertSubtreeModified(rootIt.next(), new NodeIdentifier(SUB_ROOT), 1);
        assertFalse(rootIt.hasNext());

        final var outerListIt = assertSubtreeModified(subRootIt.next(), new NodeIdentifier(OUTER_LIST), 4);
        assertFalse(subRootIt.hasNext());

        final var outerListFirstIt = assertSubtreeModified(outerListIt.next(),
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "2"), 1);

        final var outerListFirstFirstIt = assertAppeared(outerListFirstIt.next(), new NodeIdentifier(INNER_LIST), 1);
        assertFalse(outerListFirstIt.hasNext());
        assertWrite(outerListFirstFirstIt.next(), NodeIdentifierWithPredicates.of(INNER_LIST, I_ID, "a"));
        assertFalse(outerListFirstFirstIt.hasNext());

        final var outerListSecond = outerListIt.next();
        final var outerListSecondIt = assertSubtreeModified(outerListSecond,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "5"), 2);
        // O and O_ID, in some order
        assertOuterEntry(outerListSecondIt, outerListSecond, "o-5", "5");
        assertFalse(outerListSecondIt.hasNext());

        final var outerListThird = outerListIt.next();
        final var outerListThirdIt = assertSubtreeModified(outerListThird,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "4"), 2);
        // O and O_ID, in some order
        assertOuterEntry(outerListThirdIt, outerListThird, "o-4", "4");
        assertFalse(outerListThirdIt.hasNext());

        final var outerListFourth = outerListIt.next();
        final var outerListFourthIt = assertSubtreeModified(outerListFourth,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "3"), 2);
        assertFalse(outerListIt.hasNext());
        // and O and O_ID in some order
        assertOuterEntry(outerListFourthIt, outerListFourth, "o-3", "3");
        assertFalse(outerListFourthIt.hasNext());
    }

    private static void assertOuterEntry(final Iterator<DataTreeCandidateNode> it, final DataTreeCandidateNode entry,
            final String obody, final String oidBody) {
        assertEquals(obody, assertInstanceOf(LeafNode.class,
            entry.getModifiedChild(new NodeIdentifier(O)).dataAfter()).body());
        assertEquals(oidBody, assertInstanceOf(LeafNode.class,
            entry.getModifiedChild(new NodeIdentifier(O_ID)).dataAfter()).body());
        // Account for these
        assertEquals(ModificationType.WRITE, it.next().modificationType());
        assertEquals(ModificationType.WRITE, it.next().modificationType());
    }

    private static void assertCandidateThree(final DataTreeCandidateNode top) {
        assertEquals(ModificationType.SUBTREE_MODIFIED, top.modificationType());
        final var topIt = top.childNodes().iterator();
        final var rootIt = assertSubtreeModified(topIt.next(), new NodeIdentifier(ROOT), 1);
        assertFalse(topIt.hasNext());

        final var subRootIt = assertSubtreeModified(rootIt.next(), new NodeIdentifier(SUB_ROOT), 1);
        assertFalse(rootIt.hasNext());

        final var outerListIt = assertSubtreeModified(subRootIt.next(), new NodeIdentifier(OUTER_LIST), 4);
        assertFalse(subRootIt.hasNext());

        final var outerListFirstIt = assertSubtreeModified(outerListIt.next(),
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "2"), 1);
        assertWrite(outerListFirstIt.next(), new NodeIdentifier(INNER_LIST));
        assertFalse(outerListFirstIt.hasNext());

        final var outerListSecond = outerListIt.next();
        final var outerListSecondIt = assertSubtreeModified(outerListSecond,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "5"), 2);
        // O and O_ID, in some order
        assertOuterEntry(outerListSecondIt, outerListSecond, "o-5", "5");
        assertFalse(outerListSecondIt.hasNext());

        final var outerListThird = outerListIt.next();
        final var outerListThirdIt = assertSubtreeModified(outerListThird,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "4"), 2);
        // O and O_ID, in some order
        assertOuterEntry(outerListThirdIt, outerListThird, "o-4", "4");
        assertFalse(outerListThirdIt.hasNext());

        final var outerListFourth = outerListIt.next();
        final var outerListFourthIt = assertSubtreeModified(outerListFourth,
            NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, "3"), 2);
        // O and O_ID, in some order
        assertOuterEntry(outerListFourthIt, outerListFourth, "o-3", "3");
        assertFalse(outerListFourthIt.hasNext());

        assertFalse(outerListIt.hasNext());
    }

    private DataTreeCandidateNode secondModification(final int testScenarioNumber) throws Exception {
        // MERGE
        ContainerNode rootContainerNode = createRootContainerBuilder()
            .withChild(createSubRootContainerBuilder()
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(OUTER_LIST))
                    .withChild(createOuterListEntry("3", "o-3"))
                    .withChild(createOuterListEntry("4", "o-4"))
                    .withChild(createOuterListEntry("5", "o-5"))
                    .build())
                .build())
            .build();

        YangInstanceIdentifier path = YangInstanceIdentifier.of(ROOT);
        DataTreeModification modification = tree.takeSnapshot().newModification();
        modification.merge(path, rootContainerNode);

        switch (testScenarioNumber) {
            case 1:
                /* WRITE EMPTY INNER LIST */
                writeEmptyInnerList(modification, "2");
                break;
            case 2: {
                /* WRITE INNER LIST ENTRY */
                MapEntryNode innerListEntryA = createInnerListEntry("a", "i-a-2");
                path = YangInstanceIdentifier.of(ROOT, SUB_ROOT, OUTER_LIST).node(createOuterListEntryPath("2"))
                    .node(INNER_LIST).node(createInnerListEntryPath("a"));
                modification.write(path, innerListEntryA);
                break;
            }
            case 3: {
                /* WRITE INNER LIST WITH ENTRIES */
                path = YangInstanceIdentifier.of(ROOT, SUB_ROOT, OUTER_LIST).node(createOuterListEntryPath("2"))
                    .node(INNER_LIST);
                modification.write(path, createInnerListBuilder()
                    .withChild(createInnerListEntry("a", "i-a-3"))
                    .withChild(createInnerListEntry("c", "i-c"))
                    .build());
                break;
            }
            default:
                break;
        }

        // COMMIT
        modification.ready();
        tree.validate(modification);
        final var candidate = tree.prepare(modification);
        tree.commit(candidate);
        return candidate.getRootNode();
    }

    private static void writeEmptyInnerList(final DataTreeModification modification, final String outerListEntryKey) {
        YangInstanceIdentifier path = YangInstanceIdentifier.of(ROOT, SUB_ROOT, OUTER_LIST)
                .node(createOuterListEntryPath(outerListEntryKey)).node(INNER_LIST);
        modification.write(path, createInnerListBuilder().build());
    }

    private static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createRootContainerBuilder() {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(ROOT));
    }

    private static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createSubRootContainerBuilder() {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(SUB_ROOT));
    }

    private static CollectionNodeBuilder<MapEntryNode, UserMapNode> createInnerListBuilder() {
        return ImmutableNodes.newUserMapBuilder().withNodeIdentifier(NodeIdentifier.create(INNER_LIST));
    }

    private static NodeIdentifierWithPredicates createInnerListEntryPath(final String keyValue) {
        return NodeIdentifierWithPredicates.of(INNER_LIST, I_ID, keyValue);
    }

    private static NodeIdentifierWithPredicates createOuterListEntryPath(final String keyValue) {
        return NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, keyValue);
    }

    private static MapEntryNode createOuterListEntry(final String keyValue, final String leafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(OUTER_LIST, O_ID, keyValue))
            .withChild(ImmutableNodes.leafNode(O_ID, keyValue))
            .withChild(ImmutableNodes.leafNode(O, leafValue))
            .build();
    }

    private static MapEntryNode createInnerListEntry(final String keyValue, final String leafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(INNER_LIST, I_ID, keyValue))
            .withChild(ImmutableNodes.leafNode(I_ID, keyValue))
            .withChild(ImmutableNodes.leafNode(I, leafValue))
            .build();
    }

    private static Iterator<DataTreeCandidateNode> assertSubtreeModified(final DataTreeCandidateNode node,
            final PathArgument name, final int size) {
        assertEquals(name, node.name());
        assertEquals(ModificationType.SUBTREE_MODIFIED, node.modificationType());
        return assertChildren(node, size);
    }

    private static Iterator<DataTreeCandidateNode> assertAppeared(final DataTreeCandidateNode node,
            final PathArgument name, final int size) {
        assertEquals(name, node.name());
        assertEquals(ModificationType.APPEARED, node.modificationType());
        return assertChildren(node, size);
    }

    private static Iterator<DataTreeCandidateNode> assertChildren(final DataTreeCandidateNode node, final int size) {
        final var childNodes = node.childNodes();
        assertEquals(size, childNodes.size());
        final var it = childNodes.iterator();
        assertTrue(it.hasNext());
        return it;
    }

    private static void assertDelete(final DataTreeCandidateNode node, final PathArgument name) {
        assertEquals(name, node.name());
        assertEquals(ModificationType.DELETE, node.modificationType());
    }

    private static void assertWrite(final DataTreeCandidateNode node, final PathArgument name) {
        assertEquals(name, node.name());
        assertEquals(ModificationType.WRITE, node.modificationType());
    }
}
