/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal573Test {
    @Test
    public void mdsal573Test() {
        final List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal573.yang"));
        assertNotNull(generateTypes);
        assertEquals(7, generateTypes.size());

        final MethodSignature methodSignature = generateTypes.get(0).getMethodDefinitions().get(0);
        assertEquals("implementedInterface", methodSignature.getName());
    }
}
