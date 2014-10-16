/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import org.junit.Test;

public class AbstractGeneratedTypeBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyIllegalArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addProperty(null);
    }

}
