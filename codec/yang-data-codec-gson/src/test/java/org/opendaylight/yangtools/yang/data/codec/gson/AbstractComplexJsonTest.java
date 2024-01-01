/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

abstract class AbstractComplexJsonTest {
    static final QName CONT_1 = QName.create("ns:complex:json", "2014-08-11", "cont1");
    static final NodeIdentifier CONT_1_NODEID = new NodeIdentifier(CONT_1);

    private static final QName EMPTY_LEAF = QName.create(CONT_1, "empty");

    static final ContainerNode CONT1_WITH_EMPTYLEAF = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(CONT_1_NODEID)
            .addChild(ImmutableNodes.leafNode(EMPTY_LEAF, Empty.value()))
            .build();

    static EffectiveModelContext schemaContext;
    static JSONCodecFactory lhotkaCodecFactory;

    @BeforeAll
    static final void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/complexjson/yang");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext);
    }

    @AfterAll
    static final void afterClass() {
        lhotkaCodecFactory = null;
        schemaContext = null;
    }
}
