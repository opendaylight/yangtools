/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class ImmableYangDataTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    void simpleOperations() {
        final var first = ImmutableNodes.newYangDataBuilder(new YangDataName(FOO.getModule(), "a string"))
            .setContainerDataNode(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .build())
            .build();

        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .build(), first.childByArg(new NodeIdentifier(FOO)));
        assertNull(first.childByArg(new NodeIdentifier(BAR)));

        assertThat(first).isEqualTo(first);
        assertThat(first).isEqualTo(ImmutableNodes.newYangDataBuilder(new YangDataName(FOO.getModule(), "a string"))
            .setContainerDataNode(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .build())
            .build());

        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("");
        assertThat(first).isNotEqualTo(ImmutableNodes.newYangDataBuilder(new YangDataName(FOO.getModule(), "different"))
            .setContainerDataNode(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .build())
            .build());
        assertThat(first).isNotEqualTo(ImmutableNodes.newYangDataBuilder(new YangDataName(FOO.getModule(), "a string"))
            .setContainerDataNode(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .build())
            .build());

        assertEquals("""
            ImmutableYangData{name=YangDataName[module=QNameModule{ns=foo}, name=a string], \
            body=[ImmutableContainerNode{name=(foo)foo, body=[]}]}""", first.toString());
    }
}
