/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;

public class InterfaceGeneratorTest {
    @Test
    public void basicTest() throws Exception {
        assertEquals("", new InterfaceGenerator().generate(mock(Type.class)));
    }
}