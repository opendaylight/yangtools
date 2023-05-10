/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8713Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final String REV = "2017-09-06";
    private static final String BAR_YANG = """
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
        }""";
    private static final String FOO_YANG = """
        module foo {
            namespace foo;
            prefix foo;
            revision 2017-09-06;
            container root {
            }
        }""";

    @Test
    public void dataTreeCanditateValidationTest() throws Exception {
        final EffectiveModelContext context = YangParserTestUtils.parseYang(BAR_YANG, FOO_YANG);
        final LeafRefContext rootLeafRefContext = LeafRefContext.create(context);
        final DataTree inMemoryDataTree = new InMemoryDataTreeFactory().create(
            DataTreeConfiguration.DEFAULT_OPERATIONAL, context);

        final ContainerNode root = createRootContainer();
        final YangInstanceIdentifier rootPath = YangInstanceIdentifier.of(foo("root"));
        final DataTreeModification writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(rootPath, root);
        writeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private static ContainerNode createRootContainer() {
        return Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(foo("root")))
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