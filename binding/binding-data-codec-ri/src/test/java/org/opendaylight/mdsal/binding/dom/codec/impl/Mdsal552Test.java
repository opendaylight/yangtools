/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal552.norev.Mdsal552Data.OutputA;
import org.opendaylight.yang.gen.v1.mdsal552.norev.RefTestOutput;
import org.opendaylight.yang.gen.v1.mdsal552.norev.RefTestOutputBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class Mdsal552Test extends AbstractBindingCodecTest {
    private static final Absolute OUTPUT_PATH = Absolute.of(QName.create(RefTestOutput.QNAME, "ref_test"),
        RefTestOutput.QNAME);
    private static final QName OUTPUTREF = QName.create(RefTestOutput.QNAME, "outputref");

    @Test
    public void testLeafrefEnumerationToNormalized() {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(RefTestOutput.QNAME))
            .withChild(ImmutableNodes.leafNode(OUTPUTREF, OutputA.DownTest.getName()))
            .build(),
            codecContext.toNormalizedNodeRpcData(new RefTestOutputBuilder().setOutputref(OutputA.DownTest).build()));
    }

    @Test
    public void testLeafrefEnumerationFromNormalized() {
        assertEquals(new RefTestOutputBuilder().setOutputref(OutputA.DownTest).build(),
            codecContext.fromNormalizedNodeRpcData(OUTPUT_PATH, ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(RefTestOutput.QNAME))
                .withChild(ImmutableNodes.leafNode(OUTPUTREF, OutputA.DownTest.getName()))
                .build()));
    }
}
