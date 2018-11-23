/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.put.top.input.choice.list.choice.in.choice.list.ComplexViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.put.top.input.choice.list.choice.in.choice.list.ComplexViaUsesBuilder;

public class TestCopyBuilders {

    @Test
    public void testBuilderListCopy() {
        final TreeComplexUsesAugment source = new TreeComplexUsesAugmentBuilder().build();
        final ComplexViaUses viaUses = new ComplexViaUsesBuilder().build();
        final TreeComplexUsesAugment copied = new TreeComplexUsesAugmentBuilder(viaUses).build();
        assertEquals(source, copied);
    }
}
