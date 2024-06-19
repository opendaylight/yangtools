/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.ri.Types;

public class GeneratedPropertyBuilderImplTest {

    @Test
    public void generatedPropertyBuilderImplTest() {
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl = new GeneratedPropertyBuilderImpl("myPropertyName");
        generatedPropertyBuilderImpl.setValue("myValue");
        generatedPropertyBuilderImpl.setReadOnly(false);
        generatedPropertyBuilderImpl.setStatic(true);
        generatedPropertyBuilderImpl.setComment(null);
        generatedPropertyBuilderImpl.setFinal(true);
        generatedPropertyBuilderImpl.setAccessModifier(AccessModifier.PUBLIC);
        generatedPropertyBuilderImpl.setReturnType(Types.BOOLEAN);

        assertEquals("GeneratedPropertyImpl [name=myPropertyName, annotations=[], comment=null, "
            + "returnType=ConcreteTypeImpl{identifier=java.lang.Boolean}, isFinal=true, isReadOnly=false, "
            + "modifier=PUBLIC]", generatedPropertyBuilderImpl.toString());

        GeneratedProperty instance = generatedPropertyBuilderImpl.toInstance();

        assertNotNull(instance);

        assertTrue(instance.isFinal());
        assertTrue(instance.isStatic());
        assertFalse(instance.isReadOnly());
        assertEquals("myValue", instance.getValue());
        assertNull(instance.getComment());
        assertEquals(AccessModifier.PUBLIC, instance.getAccessModifier());
        assertEquals(Types.BOOLEAN, instance.getReturnType());

    }

    @Test
    public void generatedPropertyBuilderImplEqualsAndHashCodeTest() {
        final GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl =
                new GeneratedPropertyBuilderImpl("myPropertyName");
        final GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl2 =
                new GeneratedPropertyBuilderImpl("myPropertyName");
        final GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl3 =
                new GeneratedPropertyBuilderImpl("myPropertyName3");
        final GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl4 =
                new GeneratedPropertyBuilderImpl("myPropertyName");

        assertNotNull(generatedPropertyBuilderImpl);
        assertNotNull(generatedPropertyBuilderImpl2);
        assertNotNull(generatedPropertyBuilderImpl3);
        assertNotNull(generatedPropertyBuilderImpl4);

        generatedPropertyBuilderImpl.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl2.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl3.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl4.setReturnType(Types.STRING);

        assertFalse(generatedPropertyBuilderImpl.equals(null));
        assertFalse(generatedPropertyBuilderImpl.equals(new Object()));
        assertTrue(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl));
        assertTrue(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl2));
        assertFalse(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl3));
        assertFalse(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl4));

        assertTrue(generatedPropertyBuilderImpl.hashCode() == generatedPropertyBuilderImpl.hashCode());
        assertTrue(generatedPropertyBuilderImpl.hashCode() == generatedPropertyBuilderImpl2.hashCode());
        assertFalse(generatedPropertyBuilderImpl.hashCode() == generatedPropertyBuilderImpl3.hashCode());
        assertFalse(generatedPropertyBuilderImpl.hashCode() == generatedPropertyBuilderImpl4.hashCode());
    }
}
