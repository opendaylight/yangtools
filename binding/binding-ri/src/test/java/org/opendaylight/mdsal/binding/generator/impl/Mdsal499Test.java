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
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal499Test {
    @Test
    public void testSubmoduleImport() {
        List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/mdsal-499"));
        assertNotNull(generateTypes);
        assertEquals(5, generateTypes.size());
    }
}
