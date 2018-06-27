/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTypeBuilder;

public class GeneratorJavaFileTest extends BaseCompilationTest {
    private static final String FS = File.separator;
    private static final String PATH = "target/test/test-dir";

    @Test
    public void test() throws IOException {
        final GeneratedTypeBuilder gtb = new CodegenGeneratedTypeBuilder(JavaTypeName.create(
            "org.opendaylight.controller.gen", "Type4"));
        gtb.addImplementsType(Types.augmentableTypeFor(gtb));

        generateTestSources(Arrays.asList(
            createGeneratedType("org.opendaylight.controller.gen", "Type1"),
            createGeneratedType("org.opendaylight.controller.gen", "Type2"),
            createGeneratedType("org.opendaylight.controller.gen", "Type3"),
            gtb.build()), new File(PATH));

        List<String> filesList = Arrays.asList(
            new File(PATH + FS + "org" + FS + "opendaylight" + FS + "controller" + FS + "gen").list());

        // assertEquals(5, files.length);
        assertTrue(filesList.contains("Type1.java"));
        assertTrue(filesList.contains("Type2.java"));
        assertTrue(filesList.contains("Type3.java"));
        assertTrue(filesList.contains("Type4.java"));
        assertTrue(filesList.contains("Type4Builder.java"));
    }

    private static GeneratedType createGeneratedType(final String pkgName, final String name) {
        GeneratedTypeBuilder builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create(pkgName, name));
        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        return builder.build();
    }
}
