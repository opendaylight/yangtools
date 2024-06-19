/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader.GeneratorResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.Top;

public class CodecClassLoaderTest {

    private final BindingClassLoader codecClassLoader = BindingClassLoader.create(CodecClassLoaderTest.class, null);

    @ParameterizedTest(name = "Generate class within namespace: {0}")
    @MethodSource("generateClassWithinNamespaceArgs")
    void generateClassWithinNamespace(final CodecPackage pkg, final String expectedClassName) {
        final Class<?> generated = pkg.generateClass(codecClassLoader, Top.class,
            (loader, fqcn, bindingInterface) -> GeneratorResult.of(new ByteBuddy()
                .subclass(Object.class)
                .name(fqcn)
                .method(ElementMatchers.isToString())
                .intercept(FixedValue.value("test"))
                .make()));
        assertNotNull(generated);
        assertEquals(expectedClassName, generated.getName());

        final Class<?> stored = pkg.getGeneratedClass(codecClassLoader, Top.class);
        assertEquals(generated, stored);
    }

    private static Stream<Arguments> generateClassWithinNamespaceArgs() {
        final String common = "urn.opendaylight.yang.union.test.rev220428.Top";
        return Stream.of(
            Arguments.of(CodecPackage.CODEC, "org.opendaylight.yang.rt.v1.obj." + common),
            Arguments.of(CodecPackage.STREAMER, "org.opendaylight.yang.rt.v1.stream." + common),
            Arguments.of(CodecPackage.EVENT_AWARE, "org.opendaylight.yang.rt.v1.eia." + common)
        );
    }
}
