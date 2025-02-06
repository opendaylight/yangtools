/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class ControllerTest {
    @Test
    void controllerAugmentationTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            ControllerTest.class,
            "/controller-models/controller-network.yang", "/controller-models/controller-openflow.yang",
            "/ietf-models/ietf-inet-types.yang"));
        assertEquals(53, genTypes.size());
    }
}
