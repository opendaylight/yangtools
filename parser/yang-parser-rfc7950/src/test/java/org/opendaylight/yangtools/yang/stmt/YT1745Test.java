/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class YT1745Test extends AbstractYangTest {
    private static final QName ONE = QName.create("yt1475", "one");
    private static final QName TWO = QName.create("yt1475", "two");
    private static final QName THREE = QName.create("yt1475", "three");
    private static final QName ARRR = QName.create("yt1475", "arrr");


    @Test
    void effectiveConfigInInputOutputNotification() {
        final var module = assertEffectiveModel("/bugs/yt1475.yang").findModule("yt1475").orElseThrow();

        final var rpc = module.getRpcs().iterator().next();
        final var input = rpc.getInput();
        final var inOne = assertInstanceOf(LeafSchemaNode.class, input.dataChildByName(ONE));
        assertEquals(Optional.empty(), inOne.effectiveConfig());
        assertThat(inOne.asEffectiveStatement().effectiveSubstatements()).hasSize(2);

        final var inTwo = assertInstanceOf(LeafSchemaNode.class, input.dataChildByName(TWO));
        assertEquals(Optional.empty(), inTwo.effectiveConfig());
        assertThat(inTwo.asEffectiveStatement().effectiveSubstatements()).hasSize(2);

        final var inThree = assertInstanceOf(LeafSchemaNode.class, input.dataChildByName(THREE));
        assertEquals(Optional.empty(), inThree.effectiveConfig());
        assertThat(inThree.asEffectiveStatement().effectiveSubstatements()).hasSize(2);

        final var output = rpc.getOutput();
        final var outOne = assertInstanceOf(LeafSchemaNode.class, output.dataChildByName(ONE));
        assertEquals(Optional.empty(), outOne.effectiveConfig());
        assertThat(outOne.asEffectiveStatement().effectiveSubstatements()).hasSize(2);

        final var outTwo = assertInstanceOf(LeafSchemaNode.class, output.dataChildByName(TWO));
        assertEquals(Optional.empty(), outTwo.effectiveConfig());
        assertThat(outTwo.asEffectiveStatement().effectiveSubstatements()).hasSize(2);

        final var outThree = assertInstanceOf(LeafSchemaNode.class, output.dataChildByName(THREE));
        assertEquals(Optional.empty(), outThree.effectiveConfig());
        assertThat(outThree.asEffectiveStatement().effectiveSubstatements()).hasSize(2);

        assertSame(inOne, outOne);
        assertSame(inTwo, outTwo);
        assertNotSame(inThree, outThree);
    }
}
