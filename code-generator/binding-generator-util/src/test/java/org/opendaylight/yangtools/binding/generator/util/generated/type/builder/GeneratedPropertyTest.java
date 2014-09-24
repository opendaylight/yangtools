/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;

public class GeneratedPropertyTest {

    @Test
    public void testMethodsForGeneratedPropertyBuilderImpl() {
        final GeneratedPropertyBuilderImpl propertyBuilderImpl = new GeneratedPropertyBuilderImpl("testProperty");

        propertyBuilderImpl.setValue("new value");
        propertyBuilderImpl.setReadOnly(true);

        final GeneratedProperty genProperty = propertyBuilderImpl.toInstance(null);
        assertNotNull(genProperty);

        assertNotNull(propertyBuilderImpl.toString());
    }

    @Test
    public void testMethodsForGeneratedPropertyImpl() {
        final GeneratedPropertyImpl propertyImpl = new GeneratedPropertyImpl(null, "Test", null, "test property", AccessModifier.PRIVATE, null, true, true, true, "test value");

        assertEquals("test value", propertyImpl.getValue());
        assertTrue(propertyImpl.isReadOnly());
        assertNotNull(propertyImpl.toString());
    }
}
