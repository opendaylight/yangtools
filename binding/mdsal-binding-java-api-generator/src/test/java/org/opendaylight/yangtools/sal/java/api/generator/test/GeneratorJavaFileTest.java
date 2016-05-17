/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.BindingTypes;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;

public class GeneratorJavaFileTest {
    private static final String FS = File.separator;
    private static final String PATH = "target/test/test-dir";

    @Test
    public void test() throws IOException {
        final Set<GeneratedType> types = new HashSet<GeneratedType>();
        GeneratedType t1 = createGeneratedType("org.opendaylight.controller.gen", "Type1");
        GeneratedType t2 = createGeneratedType("org.opendaylight.controller.gen", "Type2");
        GeneratedType t3 = createGeneratedType("org.opendaylight.controller.gen", "Type3");
        types.add(t1);
        types.add(t2);
        types.add(t3);
        GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("org.opendaylight.controller.gen", "Type4");
        gtb.addImplementsType(Types.augmentableTypeFor(gtb));
        types.add(gtb.toInstance());
        GeneratorJavaFile generator = new GeneratorJavaFile(types);
        generator.generateToFile(new File(PATH));

        String[] files = new File(PATH + FS + "org" + FS + "opendaylight" + FS + "controller" + FS + "gen").list();
        List<String> filesList = Arrays.asList(files);

        // assertEquals(5, files.length);
        assertTrue(filesList.contains("Type1.java"));
        assertTrue(filesList.contains("Type2.java"));
        assertTrue(filesList.contains("Type3.java"));
        assertTrue(filesList.contains("Type4.java"));
        assertTrue(filesList.contains("Type4Builder.java"));
    }

    private static GeneratedType createGeneratedType(String pkgName, String name) {
        GeneratedTypeBuilder builder = new GeneratedTypeBuilderImpl(pkgName, name);
        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        return builder.toInstance();
    }

}
