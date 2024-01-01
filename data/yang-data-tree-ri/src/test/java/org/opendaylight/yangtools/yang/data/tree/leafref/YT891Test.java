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
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT891Test {
    private static final QName FOO_TOP = QName.create("urn:opendaylight:params:xml:ns:yang:foo", "2018-07-27",
        "foo-top");
    private static final QName CONTAINER_IN_LIST = QName.create(FOO_TOP, "container-in-list");
    private static final QName LIST_IN_GROUPING = QName.create(FOO_TOP, "list-in-grouping");
    private static final QName NAME = QName.create(FOO_TOP, "name");
    private static final QName REF = QName.create(FOO_TOP, "ref");
    private static final YangInstanceIdentifier FOO_TOP_ID = YangInstanceIdentifier.of(FOO_TOP);
    private static final QName BAZ_TOP = QName.create("urn:opendaylight:params:xml:ns:yang:baz", "2018-07-27",
        "baz-top");
    private static final QName BAZ_NAME = QName.create(BAZ_TOP, "name");
    private static final QName LIST_IN_CONTAINER = QName.create(BAZ_TOP, "list-in-container");
    private static final YangInstanceIdentifier BAZ_TOP_ID = YangInstanceIdentifier.of(BAZ_TOP);

    private static EffectiveModelContext schemaContext;
    private static LeafRefContext leafRefContext;

    private DataTree dataTree;

    @BeforeEach
    void before() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @BeforeAll
    static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYang("""
            module bar {
              namespace "urn:opendaylight:params:xml:ns:yang:bar";
              prefix bar;
              revision 2018-07-27;

              grouping grouping-with-list {
                list list-in-grouping {
                  key "name";
                  leaf name {
                    type leafref {
                      path "../container-in-list/name";
                    }
                  }
                  container container-in-list {
                    leaf name {
                      type string;
                    }
                  }
                }
              }
            }""", """
            module baz {
              namespace "urn:opendaylight:params:xml:ns:yang:baz";
              prefix baz;
              revision 2018-07-27;

              grouping grouping-with-leafref {
                leaf ref {
                  type leafref {
                    path "/baz-top/list-in-container/name";
                  }
                }
              }

              container baz-top {
                list list-in-container {
                  key "name";
                  leaf name {
                    type string;
                  }
                }
              }
            }""", """
            module foo {
              namespace "urn:opendaylight:params:xml:ns:yang:foo";
              prefix foo;

              import bar {
                prefix bar;
              }

              import baz {
                prefix baz;
              }

              revision 2018-07-27;

              container foo-top {
                uses bar:grouping-with-list;
                uses baz:grouping-with-leafref;
              }
            }""");
        leafRefContext = LeafRefContext.create(schemaContext);
    }

    @AfterAll
    static void afterClass() {
        schemaContext = null;
        leafRefContext = null;
    }

    @Test
    void testValid() throws Exception {
        final var writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(FOO_TOP_ID, fooTopWithList("name1"));
        writeModification.ready();
        final var writeContributorsCandidate = dataTree.prepare(writeModification);
        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test
    void testInvalid() throws Exception {
        assertThrows(LeafRefDataValidationFailedException.class, () -> {
            final var writeModification = dataTree.takeSnapshot().newModification();
            writeModification.write(FOO_TOP_ID, fooTopWithList("name2"));
            writeModification.ready();
            LeafRefValidation.validate(dataTree.prepare(writeModification), leafRefContext);
        });
    }

    @Test
    void testGroupingWithLeafrefValid() throws Exception {
        final var writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(BAZ_TOP_ID, bazTop());
        writeModification.write(FOO_TOP_ID, fooTopWithRef("name1"));
        writeModification.ready();
        LeafRefValidation.validate(dataTree.prepare(writeModification), leafRefContext);
    }

    @Test
    void testGroupingWithLeafrefInvalid() throws Exception {
        assertThrows(LeafRefDataValidationFailedException.class, () -> {
            final var writeModification = dataTree.takeSnapshot().newModification();
            writeModification.write(BAZ_TOP_ID, bazTop());
            writeModification.write(FOO_TOP_ID, fooTopWithRef("name3"));
            writeModification.ready();
            LeafRefValidation.validate(dataTree.prepare(writeModification), leafRefContext);
        });
    }

    private static ContainerNode fooTopWithList(final String refValue) {
        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO_TOP))
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(LIST_IN_GROUPING))
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_IN_GROUPING, NAME, "name1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "name1"))
                        .withChild(ImmutableNodes.newContainerBuilder()
                            .withNodeIdentifier(new NodeIdentifier(CONTAINER_IN_LIST))
                            .withChild(ImmutableNodes.leafNode(NAME, refValue))
                            .build())
                        .build())
                    .build())
                .build();
    }

    private static ContainerNode fooTopWithRef(final String refValue) {
        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO_TOP))
                .withChild(ImmutableNodes.leafNode(REF, refValue))
                .build();
    }

    private static ContainerNode bazTop() {
        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAZ_TOP))
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(LIST_IN_CONTAINER))
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_IN_CONTAINER, BAZ_NAME, "name1"))
                        .withChild(ImmutableNodes.leafNode(BAZ_NAME, "name1"))
                        .build())
                    .build())
                .build();
    }
}
