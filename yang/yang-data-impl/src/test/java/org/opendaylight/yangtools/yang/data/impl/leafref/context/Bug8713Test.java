/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref.context;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefValidatation;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8713Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final String REV = "2017-09-06";

    @Test
    public void dataTreeCanditateValidationTest() throws Exception {
        final SchemaContext context = YangParserTestUtils.parseYangSources("/bug8713/");
        final LeafRefContext rootLeafRefContext = LeafRefContext.create(context);
        final TipProducingDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance()
                .create(DataTreeConfiguration.DEFAULT_OPERATIONAL);
        inMemoryDataTree.setSchemaContext(context);

        final ContainerNode root = createRootContainer();
        final YangInstanceIdentifier rootPath = YangInstanceIdentifier.of(foo("root"));
        final DataTreeModification writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(rootPath, root);
        writeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LeafRefValidatation.validate(writeContributorsCandidate, rootLeafRefContext);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private ContainerNode createRootContainer() {
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