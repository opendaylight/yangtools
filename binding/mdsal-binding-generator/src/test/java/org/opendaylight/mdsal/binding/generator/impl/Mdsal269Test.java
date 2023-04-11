/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal269Test {
    @Test
    public void mdsal269Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal269.yang"));
        assertEquals(4, generateTypes.size());

        final GeneratedType mplsLabelType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabel")).findFirst().orElseThrow();

        assertTrue(mplsLabelType instanceof GeneratedTransferObject);
        final GeneratedTransferObject gto = (GeneratedTransferObject) mplsLabelType;
        final Iterator<GeneratedProperty> it = gto.getEqualsIdentifiers().iterator();
        final GeneratedProperty special = it.next();
        final GeneratedProperty general = it.next();
        assertFalse(it.hasNext());

        assertEquals("mplsLabelGeneralUse", general.getName());
        assertEquals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabelGeneralUse",
            general.getReturnType().getFullyQualifiedName());

        assertEquals("mplsLabelSpecialPurpose", special.getName());
        assertEquals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabelSpecialPurposeValue",
            special.getReturnType().getFullyQualifiedName());
    }
}
