/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;

class GeneratorJavaFileTest extends BaseCompilationTest {
    private static final Path PATH = Path.of("target", "test", "test-dir");

    @Test
    void test() throws IOException {
        final GeneratedTypeBuilder gtb = new CodegenGeneratedTypeBuilder(JavaTypeName.create(
            "org.opendaylight.controller.gen", "Type4"));
        gtb.addImplementsType(BindingTypes.augmentable(gtb));

        generateTestSources(Arrays.asList(
            createGeneratedType("org.opendaylight.controller.gen", "Type1"),
            createGeneratedType("org.opendaylight.controller.gen", "Type2"),
            createGeneratedType("org.opendaylight.controller.gen", "Type3"),
            gtb.build()), PATH);

        try (var dir = Files.list(PATH.resolve(Path.of("org", "opendaylight", "controller", "gen")))) {
            assertEquals(List.of("Type1.java", "Type2.java", "Type3.java", "Type4.java", "Type4Builder.java"),
                dir.map(Path::getFileName).map(Path::toString).sorted().toList());
        }
    }

    @NonNullByDefault
    private static GeneratedType createGeneratedType(final String pkgName, final String name) {
        GeneratedTypeBuilder builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create(pkgName, name));
        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        return builder.build();
    }
}
