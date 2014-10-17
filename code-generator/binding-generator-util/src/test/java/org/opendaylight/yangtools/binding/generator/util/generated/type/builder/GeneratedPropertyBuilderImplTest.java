/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

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

        assertEquals(
                "GeneratedPropertyImpl [name=myPropertyName, annotations=[], comment=null, returnType=Type (java.lang.Boolean), isFinal=true, isReadOnly=false, modifier=PUBLIC]",
                generatedPropertyBuilderImpl.toString());

        GeneratedProperty instance = generatedPropertyBuilderImpl.toInstance(null);

        assertNotNull(instance);

        assertTrue(instance.isFinal());
        assertTrue(instance.isStatic());
        assertFalse(instance.isReadOnly());
        assertEquals("myValue", instance.getValue());
        assertEquals(null, instance.getComment());
        assertEquals(AccessModifier.PUBLIC, instance.getAccessModifier());
        assertEquals(Types.BOOLEAN, instance.getReturnType());

    }

    @Test
    public void generatedPropertyBuilderImplEqualsAndHashCodeTest() {
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl = new GeneratedPropertyBuilderImpl("myPropertyName");
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl2 = new GeneratedPropertyBuilderImpl("myPropertyName");
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl3 = new GeneratedPropertyBuilderImpl("myPropertyName3");
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl4 = new GeneratedPropertyBuilderImpl("myPropertyName");

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
