/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;

public class GeneratedTOBuilderTest {

    @Test
    public void testBuilder() {
        final GeneratedTOBuilder genTypeBuilder = new GeneratedTOBuilderImpl(
                "org.opendaylight.controller", "AnnotClassCache");

        genTypeBuilder.setSUID(genTypeBuilder.addProperty("SUID"));
        genTypeBuilder.addMethod("addCount");

        GeneratedTransferObject genTO = genTypeBuilder.toInstance();
        genTypeBuilder.setExtendsType(genTO);

        GeneratedPropertyBuilder property = genTypeBuilder
                .addProperty("customProperty");
        genTypeBuilder.addHashIdentity(property);

        genTypeBuilder.addEqualsIdentity(property);

        genTypeBuilder.addToStringProperty(property);

        assertNotNull(genTO);
        assertNotNull(genTO.getProperties());
    }

    @Test
    public void testToString() {
        final GeneratedTOBuilder genTypeBuilder = new GeneratedTOBuilderImpl(
                "org.opendaylight.controller", "AnnotClassCache");
        String toString = genTypeBuilder.toString();
        assertTrue(toString.contains("GeneratedTransferObject"));
    }

    @Test
    public void testTransferBuilderToString() {
        final GeneratedTOBuilder genTypeBuilder1 = new GeneratedTOBuilderImpl(
                "org.opendaylight.controller", "AnnotClassCache");

        genTypeBuilder1.setTypedef(true);
        GeneratedTransferObject genTO = genTypeBuilder1.toInstance();
        String toString = genTO.toString();
        assertFalse(toString.contains("GeneratedTransferObject"));

        final GeneratedTOBuilder genTypeBuilder2 = new GeneratedTOBuilderImpl(
                "org.opendaylight.controller", "AnnotClassCache");

        genTypeBuilder2.setTypedef(false);
        genTO = genTypeBuilder2.toInstance();
        toString = genTO.toString();

        assertTrue(toString.contains("GeneratedTransferObject"));
    }
}
