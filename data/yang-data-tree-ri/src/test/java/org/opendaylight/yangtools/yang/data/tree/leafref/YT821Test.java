/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT821Test {
    private static final QName ROOT = QName.create("urn:opendaylight:params:xml:ns:yang:foo", "2018-07-18", "root");
    private static final QName FOO = QName.create(ROOT, "foo");
    private static final QName BAR = QName.create(ROOT, "bar");
    private static final QName NAME = QName.create(ROOT, "name");
    private static final QName CONTAINER_IN_LIST = QName.create(ROOT, "container-in-list");
    private static final QName REF_FROM_AUG = QName.create(ROOT, "ref-from-aug");
    private static final QName CONTAINER_FROM_AUG = QName.create(ROOT, "container-from-aug");
    private static final QName REF_IN_CONTAINER = QName.create(ROOT, "ref-in-container");
    private static final YangInstanceIdentifier ROOT_ID = YangInstanceIdentifier.of(ROOT);

    private static EffectiveModelContext schemaContext;
    private static LeafRefContext leafRefContext;

    private DataTree dataTree;

    @BeforeAll
    static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYang("""
            module yt821 {
              namespace "urn:opendaylight:params:xml:ns:yang:foo";
              prefix foo;
              revision 2018-07-18;

              container root {
                list foo {
                  key name;
                  leaf name {
                    type string;
                  }
                }

                list bar {
                  key name;
                  leaf name {
                    type string;
                  }

                  container container-in-list {
                  }
                }
              }

              augment /root/bar/container-in-list {
                leaf ref-from-aug {
                  type leafref {
                    path "/root/foo/name";
                  }
                }
              }

              augment /root/bar/ {
                container container-from-aug {
                  leaf ref-in-container {
                    type leafref {
                      path "/root/foo/name";
                    }
                  }
                }
              }
            }""");
        leafRefContext = LeafRefContext.create(schemaContext);
    }

    @AfterAll
    static void afterClass() {
        schemaContext = null;
        leafRefContext = null;
    }

    @BeforeEach
    void before() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    void testValidRefFromAugmentation() throws Exception {
        final var writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, refFromAug("foo1"));
        writeModification.ready();
        final var writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test
    void testInvalidRefFromAugmentation() throws Exception {
        assertThrows(LeafRefDataValidationFailedException.class, () -> {
            final var writeModification = dataTree.takeSnapshot().newModification();
            writeModification.write(ROOT_ID, refFromAug("foo2"));
            writeModification.ready();
            final var writeContributorsCandidate = dataTree.prepare(writeModification);

            LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        });
    }

    @Test
    void testValidRefInContainerFromAugmentation() throws Exception {
        final var writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, refInContainer("foo1"));
        writeModification.ready();
        final var writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test
    void testInvalidRefInContainerFromAugmentation() throws Exception {
        assertThrows(LeafRefDataValidationFailedException.class, () -> {
            final var writeModification = dataTree.takeSnapshot().newModification();
            writeModification.write(ROOT_ID, refInContainer("foo2"));
            writeModification.ready();
            final var writeContributorsCandidate = dataTree.prepare(writeModification);

            LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        });
    }

    private static ContainerNode refFromAug(final String refValue) {
        return Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT))
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(FOO))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, "foo1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "foo1"))
                        .build())
                    .build())
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAR))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(BAR, NAME, "bar1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "bar1"))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(new NodeIdentifier(CONTAINER_IN_LIST))
                            .withChild(ImmutableNodes.leafNode(REF_FROM_AUG, refValue))
                            .build())
                        .build())
                    .build())
                .build();
    }

    private static ContainerNode refInContainer(final String refValue) {
        return Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT))
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(FOO))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, "foo1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "foo1"))
                        .build())
                    .build())
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAR))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(BAR, NAME, "bar1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "bar1"))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(new NodeIdentifier(CONTAINER_FROM_AUG))
                            .withChild(ImmutableNodes.leafNode(REF_IN_CONTAINER, refValue))
                            .build())
                        .build())
                    .build())
                .build();
    }
}
