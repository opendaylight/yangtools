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

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class GeneratedTypeBuilderTest {

    @Test
    public void testCreateGeneratedTypeBuilderImpl() {
        final GeneratedTypeBuilderImpl typeBuilderImpl = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestGenType");
        assertNotNull(typeBuilderImpl);
    }

    @Test
    public void testSetterMethodsForGeneratedTypeBuilderImpl() {
        final GeneratedTypeBuilderImpl typeBuilderImpl = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestGenType");

        typeBuilderImpl.setAbstract(true);
        typeBuilderImpl.setDescription("abstract generated type");
        typeBuilderImpl.setModuleName("test-module");
        typeBuilderImpl.setReference("http://tools.ietf.org/html/rfc6020");
        typeBuilderImpl.setSchemaPath(SchemaPath.ROOT.getPathFromRoot());

        final GeneratedType genType = typeBuilderImpl.toInstance();

        assertEquals("abstract generated type", genType.getDescription());
        assertEquals("test-module", genType.getModuleName());
        assertEquals("http://tools.ietf.org/html/rfc6020", genType.getReference());
        assertEquals(SchemaPath.ROOT.getPathFromRoot(), genType.getSchemaPath());
        assertNotNull(typeBuilderImpl.toString());
    }
}
