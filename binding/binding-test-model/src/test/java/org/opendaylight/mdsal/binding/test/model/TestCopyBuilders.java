/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.put.top.input.choice.list.choice.in.choice.list.ComplexViaUsesBuilder;

public class TestCopyBuilders {
    @Test
    public void testBuilderListCopy() {
        final var source = new TreeComplexUsesAugmentBuilder().build();
        final var viaUses = new ComplexViaUsesBuilder().build();
        final var copied = new TreeComplexUsesAugmentBuilder(viaUses).build();
        assertEquals(source, copied);
    }
}
