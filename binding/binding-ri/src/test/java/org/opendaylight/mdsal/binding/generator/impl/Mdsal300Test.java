/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal300Test {
    @Test
    public void testActionsAndAugmentsTest() {
        List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/mdsal-300"));
        assertNotNull(generateTypes);
        assertEquals(19, generateTypes.size());
    }
}
