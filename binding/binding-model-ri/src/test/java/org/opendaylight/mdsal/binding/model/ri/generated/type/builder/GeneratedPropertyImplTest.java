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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

public class GeneratedPropertyImplTest {

    @Test
    public void generatedPropertyImplTest() {
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl = new GeneratedPropertyBuilderImpl("myPropertyName");
        generatedPropertyBuilderImpl.setValue("myValue");
        generatedPropertyBuilderImpl.setReadOnly(false);
        generatedPropertyBuilderImpl.setStatic(true);
        generatedPropertyBuilderImpl.setComment(TypeMemberComment.contractOf("myComment"));
        generatedPropertyBuilderImpl.setFinal(true);
        generatedPropertyBuilderImpl.setAccessModifier(AccessModifier.PUBLIC);
        generatedPropertyBuilderImpl.setReturnType(Types.BOOLEAN);

        GeneratedProperty instance = generatedPropertyBuilderImpl.toInstance();

        assertNotNull(instance);

        assertTrue(instance.isFinal());
        assertTrue(instance.isStatic());
        assertFalse(instance.isReadOnly());
        assertEquals("myValue", instance.getValue());
        assertEquals(TypeMemberComment.contractOf("myComment"), instance.getComment());
        assertEquals(AccessModifier.PUBLIC, instance.getAccessModifier());
        assertEquals(Types.BOOLEAN, instance.getReturnType());

        assertEquals("GeneratedPropertyImpl [name=myPropertyName, annotations=[], "
            + "comment=TypeMemberComment{contract=myComment}, "
            + "returnType=ConcreteTypeImpl{identifier=java.lang.Boolean}, isFinal=true, isReadOnly=false, "
            + "modifier=PUBLIC]", instance.toString());
    }

    @Test
    public void generatedPropertyImplEqualsAndHashCodeTest() {
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl = new GeneratedPropertyBuilderImpl("myPropertyName");
        GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl2 = new GeneratedPropertyBuilderImpl("myPropertyName");
        final GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl3 =
                new GeneratedPropertyBuilderImpl("myPropertyName3");
        final GeneratedPropertyBuilderImpl generatedPropertyBuilderImpl4 =
                new GeneratedPropertyBuilderImpl("myPropertyName");

        generatedPropertyBuilderImpl.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl2.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl3.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl4.setReturnType(Types.STRING);

        final GeneratedProperty property = generatedPropertyBuilderImpl.toInstance();
        final GeneratedProperty property2 = generatedPropertyBuilderImpl2.toInstance();
        final GeneratedProperty property3 = generatedPropertyBuilderImpl3.toInstance();
        final GeneratedProperty property4 = generatedPropertyBuilderImpl4.toInstance();

        assertNotNull(property);
        assertNotNull(property2);
        assertNotNull(property3);
        assertNotNull(property4);

        assertFalse(property.equals(null));
        assertFalse(property.equals(new Object()));
        assertTrue(property.equals(property));
        assertTrue(property.equals(property2));
        assertFalse(property.equals(property3));
        assertFalse(property.equals(property4));

        assertTrue(property.hashCode() == property.hashCode());
        assertTrue(property.hashCode() == property2.hashCode());
        assertFalse(property.hashCode() == property3.hashCode());
        assertFalse(property.hashCode() == property4.hashCode());
    }

}
