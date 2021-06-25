/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal500Test {
    @Test
    public void testAugmentedAction() {
        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResource("/mdsal500.yang"));
        assertNotNull(types);
        assertEquals(3, types.size());

        final GeneratedType rpcService = types.get(2);
        assertEquals("Mdsal500Service", rpcService.getName());
        final List<MethodSignature> methods = rpcService.getMethodDefinitions();
        assertEquals(1, methods.size());

        final MethodSignature signature = methods.get(0);
        assertEquals("switch$", signature.getName());
    }
}
