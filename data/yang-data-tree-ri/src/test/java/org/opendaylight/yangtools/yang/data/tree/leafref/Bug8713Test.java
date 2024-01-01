/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug8713Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final String REV = "2017-09-06";

    @Test
    void dataTreeCanditateValidationTest() throws Exception {
        final var context = YangParserTestUtils.parseYang("""
            module bar {
              namespace bar;
              prefix bar;

              import foo { prefix foo; revision-date 2017-09-06; }
              revision 2017-09-06;

              augment "/foo:root" {
                leaf ref {
                  type leafref {
                    path "../target" ;
                  }
                }
                leaf target {
                  type string;
                }
              }
            }""", """
            module foo {
              namespace foo;
              prefix foo;
              revision 2017-09-06;

              container root {
              }
            }""");
        final var rootLeafRefContext = LeafRefContext.create(context);
        final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
            DataTreeConfiguration.DEFAULT_OPERATIONAL, context);

        final var root = createRootContainer();
        final var rootPath = YangInstanceIdentifier.of(foo("root"));
        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(rootPath, root);
        writeModification.ready();

        final var writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private static ContainerNode createRootContainer() {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(foo("root")))
                .withChild(ImmutableNodes.leafNode(bar("target"), "target value"))
                .withChild(ImmutableNodes.leafNode(bar("ref"), "target value")).build();
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, REV, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, REV, localName);
    }
}