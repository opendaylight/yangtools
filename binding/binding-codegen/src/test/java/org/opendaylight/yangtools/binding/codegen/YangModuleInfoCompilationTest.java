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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertRegularFile;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test correct generation of YangModuleInfo class.
 */
class YangModuleInfoCompilationTest extends BaseCompilationTest {
    private static final String BASE_PKG = "org.opendaylight.yang.svc.v1";

    @Test
    void compilationTest() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.GENERATOR_OUTPUT_DIR.resolve("yang");
        Files.createDirectory(sourcesOutputDir);
        final var compiledOutputDir = CompilationTestUtils.COMPILER_OUTPUT_DIR.resolve("yang");
        Files.createDirectory(compiledOutputDir);

        final var resourceDirPath = "/yang-module-info";
        final var context = YangParserTestUtils.parseYangResourceDirectory(resourceDirPath);
        final var codegen = new JavaFileGenerator(Map.of()).generateFiles(context, Set.copyOf(context.getModules()),
                (module, representation) -> Optional.of(resourceDirPath + File.separator + module.getName()
                    + YangConstants.RFC6020_YANG_FILE_EXTENSION));

        assertEquals(15, codegen.size());
        assertEquals(14, codegen.row(GeneratedFileType.SOURCE).size());
        assertEquals(1, codegen.row(GeneratedFileType.RESOURCE).size());

        for (var entry : codegen.row(GeneratedFileType.SOURCE).entrySet()) {
            final var path = sourcesOutputDir.resolve(
                entry.getKey().getPath().replace(GeneratedFilePath.SEPARATOR, File.separatorChar));

            Files.createDirectories(path.getParent());
            try (var out = Files.newOutputStream(path)) {
                entry.getValue().writeBody(out);
            }
        }

        // Test if $YangModuleInfoImpl.java file is generated
        final var parent = sourcesOutputDir.resolve(
            Path.of("org", "opendaylight", "yang", "svc", "v1", "yang", "test", "main", "rev140630"));
        assertRegularFile(parent, "YangModuleInfoImpl.java");

        assertEquals("""
            package org.opendaylight.yang.svc.v1.yang.test.main.rev140630;

            import com.google.common.collect.ImmutableSet;
            import java.lang.Override;
            import java.lang.String;
            import java.util.HashSet;
            import java.util.Set;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
            import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
            import org.opendaylight.yangtools.yang.common.QName;

            /**
             * The {@link ResourceYangModuleInfo} for {@code main-module} module.
             */
            @javax.annotation.processing.Generated("mdsal-binding-generator")
            public final class YangModuleInfoImpl extends ResourceYangModuleInfo {
                private static final @NonNull QName NAME = QName.create("yang:test:main", "2014-06-30", "main-module")\
            .intern();

                /**
                 * The singleton instance.
                 */
                public static final @NonNull YangModuleInfo INSTANCE = new YangModuleInfoImpl();

                private final @NonNull ImmutableSet<YangModuleInfo> importedModules;

                private YangModuleInfoImpl() {
                    Set<YangModuleInfo> set = new HashSet<>();
                    set.add(org.opendaylight.yang.svc.v1.yang.test._import.rev131119.YangModuleInfoImpl.INSTANCE);
                    set.add(Submodule1Info.INSTANCE);
                    set.add(Submodule2Info.INSTANCE);
                    set.add(Submodule3Info.INSTANCE);
                    importedModules = ImmutableSet.copyOf(set);
                }
               \s
                @Override
                public QName getName() {
                    return NAME;
                }
               \s
                @Override
                protected String resourceName() {
                    return "/yang-module-info/main-module.yang";
                }
               \s
                @Override
                public ImmutableSet<YangModuleInfo> getImportedModules() {
                    return importedModules;
                }
               \s
                private static final class Submodule1Info extends ResourceYangModuleInfo {
                    private final @NonNull QName NAME = QName.create("yang:test:main", "2014-04-02", "submodule1")\
            .intern();
               \s
                    static final @NonNull YangModuleInfo INSTANCE = new Submodule1Info();
               \s
                    private final @NonNull ImmutableSet<YangModuleInfo> importedModules;
               \s
                    private Submodule1Info() {
                        importedModules = ImmutableSet.of();
                    }
                   \s
                    @Override
                    public QName getName() {
                        return NAME;
                    }
                   \s
                    @Override
                    protected String resourceName() {
                        return "/yang-module-info/submodule1.yang";
                    }
                   \s
                    @Override
                    public ImmutableSet<YangModuleInfo> getImportedModules() {
                        return importedModules;
                    }
                }
               \s
                private static final class Submodule2Info extends ResourceYangModuleInfo {
                    private final @NonNull QName NAME = QName.create("yang:test:main", "2014-06-30", "submodule2")\
            .intern();
               \s
                    static final @NonNull YangModuleInfo INSTANCE = new Submodule2Info();
               \s
                    private final @NonNull ImmutableSet<YangModuleInfo> importedModules;
               \s
                    private Submodule2Info() {
                        importedModules = ImmutableSet.of();
                    }
                   \s
                    @Override
                    public QName getName() {
                        return NAME;
                    }
                   \s
                    @Override
                    protected String resourceName() {
                        return "/yang-module-info/submodule2.yang";
                    }
                   \s
                    @Override
                    public ImmutableSet<YangModuleInfo> getImportedModules() {
                        return importedModules;
                    }
                }
               \s
                private static final class Submodule3Info extends ResourceYangModuleInfo {
                    private final @NonNull QName NAME = QName.create("yang:test:main", "2014-06-30", "submodule3")\
            .intern();
               \s
                    static final @NonNull YangModuleInfo INSTANCE = new Submodule3Info();
               \s
                    private final @NonNull ImmutableSet<YangModuleInfo> importedModules;
               \s
                    private Submodule3Info() {
                        importedModules = ImmutableSet.of();
                    }
                   \s
                    @Override
                    public QName getName() {
                        return NAME;
                    }
                   \s
                    @Override
                    protected String resourceName() {
                        return "/yang-module-info/submodule3.yang";
                    }
                   \s
                    @Override
                    public ImmutableSet<YangModuleInfo> getImportedModules() {
                        return importedModules;
                    }
                }

                /**
                 * Create an interned {@link QName} with specified {@code localName} and namespace/revision of this
                 * module.
                 *
                 * @param localName local name
                 * @return A QName
                 * @throws NullPointerException if {@code localName} is {@code null}
                 * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
                 */
                public static @NonNull QName qnameOf(final String localName) {
                    return QName.create(NAME, localName).intern();
                }
            }
            """, Files.readString(parent.resolve("YangModuleInfoImpl.java")));

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        // Create URLClassLoader
        final var urls = new URL[] {
            compiledOutputDir.toUri().toURL(),
            Path.of(System.getProperty("user.dir")).toUri().toURL()
        };
        ClassLoader loader = new URLClassLoader(urls);

        // Load class
        Class<?> yangModuleInfoClass =
            Class.forName(BASE_PKG + ".yang.test.main.rev140630.YangModuleInfoImpl", true, loader);

        // Test generated $YangModuleInfoImpl class
        assertFalse(yangModuleInfoClass.isInterface());

        final var instance = yangModuleInfoClass.getDeclaredField("INSTANCE");
        assertEquals(YangModuleInfo.class, instance.getType());

        final var yangModuleInfo = assertInstanceOf(YangModuleInfo.class, instance.get(null));

        // Test getImportedModules method
        final var getImportedModules = assertContainsMethod(yangModuleInfoClass, ImmutableSet.class,
            "getImportedModules");
        final var importedModules = assertInstanceOf(ImmutableSet.class, getImportedModules.invoke(yangModuleInfo));

        YangModuleInfo infoImport = null;
        YangModuleInfo infoSub1 = null;
        YangModuleInfo infoSub2 = null;
        YangModuleInfo infoSub3 = null;
        for (var importedModule : importedModules) {
            final var moduleInfo = assertInstanceOf(YangModuleInfo.class, importedModule);
            String name = moduleInfo.getName().getLocalName();

            switch (name) {
                case "import-module" -> infoImport = moduleInfo;
                case "submodule1" -> infoSub1 = moduleInfo;
                case "submodule2" -> infoSub2 = moduleInfo;
                case "submodule3" -> infoSub3 = moduleInfo;
                default -> {
                    // no-op
                }
            }
        }
        assertNotNull(infoImport);
        assertThat(infoImport.getYangTextCharSource().readFirstLine()).startsWith("module import-module");
        assertNotNull(infoSub1);
        assertThat(infoSub1.getYangTextCharSource().readFirstLine()).startsWith("submodule submodule1");
        assertNotNull(infoSub2);
        assertThat(infoSub2.getYangTextCharSource().readFirstLine()).startsWith("submodule submodule2");
        assertNotNull(infoSub3);
        assertThat(infoSub3.getYangTextCharSource().readFirstLine()).startsWith("submodule submodule3");

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void generateTestSourcesWithAdditionalConfig() throws Exception {
        final var context = YangParserTestUtils.parseYangResourceDirectory("/yang-module-info");
        final var codegen = new JavaFileGenerator(Map.of("test", "test"));
        final var files = codegen.generateFiles(context,
            Set.copyOf(context.getModules()), (module, representation) -> Optional.of(module.getName()));
        assertEquals(15, files.size());
        assertEquals(14, files.row(GeneratedFileType.SOURCE).size());
        assertEquals(1, files.row(GeneratedFileType.RESOURCE).size());
    }

    private static Method assertContainsMethod(final Class<?> clazz, final Class<?> returnType, final String name,
            final Class<?>... args) {
        final var method = assertDoesNotThrow(() -> clazz.getDeclaredMethod(name, args));
        assertEquals(returnType, method.getReturnType());
        return method;
    }
}
