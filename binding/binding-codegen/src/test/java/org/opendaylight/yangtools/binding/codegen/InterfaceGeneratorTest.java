/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.codegen.FileSearchUtil.getFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InterfaceGeneratorTest extends BaseCompilationTest {
    private static Path SOURCES;
    private static Map<String, Path> FILES;

    @BeforeAll
    static void beforeAll() {
        SOURCES = CompilationTestUtils.generatorOutput("status-means-deprecated");
        assertEquals(4, generateTestSources("/compilation/bug586", SOURCES).size());
        FILES = getFiles(SOURCES);
        assertEquals(17, FILES.size());
    }

    @AfterAll
    static void afterAll() throws Exception {
        CompilationTestUtils.cleanUp(SOURCES);
        SOURCES = null;
        FILES = null;
    }

    private static AbstractStringAssert<?> assertFileContent(final String name) {
        final var file = FILES.get(name);
        assertNotNull(file, name + " not found");
        return assertThat(assertDoesNotThrow(() -> Files.readString(file)));
    }

    @Test
    void builderTemplateDeprecatedListenerMethodTest() {
        assertFileContent("Services.java").contains("""

                /**
                 * {@return {@code Map<ServiceKey, Service>} service, or {@code null} if it is not present}
                 */
                @Deprecated(forRemoval = true)
                @Nullable Map<ServiceKey, Service> getService();

                /**
                 * {@return {@code Map<ServiceKey, Service>} service, or an empty list if it is not present}
                 */
                @Deprecated(forRemoval = true)
                default @NonNull Map<ServiceKey, Service> nonnullService() {
                    return CodeHelpers.nonnull(getService());
                }

            """);
        assertFileContent("Service.java").contains("""
             */
            @Deprecated(forRemoval = true)
            @Generated("mdsal-binding-generator")
            public interface Service
                extends ChildOf<Services>,
                        EntryObject<Service, ServiceKey> {
            """);
    }

    @Test
    void builderTemplateGenerateObsoleteListenerMethodTest() {
        assertFileContent("FooData.java").endsWith("""

                /**
                 * {@return {@code Services} services, or {@code null} if it is not present}
                 */
                @Deprecated
                Services getServices();

                /**
                 * {@return {@code Services} services, or an empty instance if it is not present}
                 */
                @Deprecated
                @NonNull Services nonnullServices();


                @Override
                default Class<org.opendaylight.yang.gen.v1.urn.yang.foo.rev140328.FooData> implementedInterface() {
                    return org.opendaylight.yang.gen.v1.urn.yang.foo.rev140328.FooData.class;
                }
            }
            """);
    }
}
