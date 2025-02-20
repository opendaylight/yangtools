/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public final class CompilationTestUtils {
    static final String BASE_PKG = "org.opendaylight.yang.gen.v1";

    static final Path TEST_DIR = Path.of("target", "test");
    static final Path GENERATOR_OUTPUT_DIR = TEST_DIR.resolve("src");
    static final Path COMPILER_OUTPUT_DIR = TEST_DIR.resolve("bin");

    static final String AUGMENTATION = "interface org.opendaylight.yangtools.binding.Augmentation";
    static final Path BASE_PATH = Path.of("org", "opendaylight", "yang", "gen", "v1");
    static final Path BASE_SVC_PATH = Path.of("org", "opendaylight", "yang", "svc", "v1");
    static final Path NS_TEST = BASE_PATH.resolve(Path.of("urn", "opendaylight", "test", "rev131008"));
    static final Path NS_SVC_TEST = BASE_SVC_PATH.resolve(Path.of("urn", "opendaylight", "test", "rev131008"));
    static final Path NS_FOO = BASE_PATH.resolve(Path.of("urn", "opendaylight", "foo", "rev131008"));
    static final Path NS_SVC_FOO = BASE_SVC_PATH.resolve(Path.of("urn", "opendaylight", "foo", "rev131008"));
    static final Path NS_BAR = BASE_PATH.resolve(Path.of("urn", "opendaylight", "bar", "rev131008"));
    static final Path NS_SVC_BAR = BASE_SVC_PATH.resolve(Path.of("urn", "opendaylight", "bar", "rev131008"));
    static final Path NS_BAZ = BASE_PATH.resolve(Path.of("urn", "opendaylight", "baz", "rev131008"));
    static final Path NS_SVC_BAZ = BASE_SVC_PATH.resolve(Path.of("urn", "opendaylight", "baz", "rev131008"));
    static final Path NS_BUG5882 = BASE_PATH.resolve(Path.of("urn", "yang", "foo", "rev160102"));

    private CompilationTestUtils() {
        // Hidden on purpose
    }

    static Path compilerOutput(final String name) {
        return createDirectory(COMPILER_OUTPUT_DIR.resolve(name));
    }

    static Path generatorOutput(final String name) {
        return createDirectory(GENERATOR_OUTPUT_DIR.resolve(name));
    }

    private static Path createDirectory(final Path path) {
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return path;
    }

    /**
     * Method to clean resources. It is manually called at the end of each test instead of marking it with @After
     * annotation to prevent removing generated code if test fails.
     */
    static void cleanUp(final Path... resourceDirs) throws IOException {
        for (var resourceDir : resourceDirs) {
            if (Files.exists(resourceDir)) {
                deleteTestDir(resourceDir);
            }
        }
    }

    /**
     * Asserts that class contains field with given name and type.
     *
     * @param clazz class to test
     * @param name field name
     * @param type field type
     * @return field with given name if present in class
     */
    static Field assertContainsField(final Class<?> clazz, final String name, final Class<?> type) {
        try {
            Field field = clazz.getDeclaredField(name);
            assertEquals(type, field.getType());
            return field;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Field " + name + " does not exist in class " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Asserts that class contains field with given name and value. Method tries to create new instance of class
     * and get value of field. If class constructor contains any arguments, class is instantiated with null values.
     *
     * @param clazz class to test
     * @param name name of field
     * @param returnType return type of field
     * @param expectedValue expected value of field
     * @param constructorArgs constructor arguments of class to test
     */
    static void assertContainsFieldWithValue(final Class<?> clazz, final String name, final Class<?> returnType,
            final Object expectedValue, final Class<?>... constructorArgs) {
        Object[] initargs = null;
        if (constructorArgs != null && constructorArgs.length > 0) {
            initargs = new Object[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                initargs[i] = null;
            }
        }
        assertContainsFieldWithValue(clazz, name, returnType, expectedValue, constructorArgs, initargs);
    }

    /**
     * Asserts that class contains field with given name, return type and value.
     *
     * @param clazz class to test
     * @param name name of field
     * @param returnType return type of field
     * @param expectedValue expected value of field
     * @param constructorArgs array of constructor arguments classes
     * @param initargs array of constructor values
     */
    static void assertContainsFieldWithValue(final Class<?> clazz, final String name, final Class<?> returnType,
            final Object expectedValue, final Class<?>[] constructorArgs, final Object... initargs) {
        final var field = assertContainsField(clazz, name, returnType);
        field.setAccessible(true);

        final Object obj;
        if ((field.getModifiers() & Modifier.STATIC) == 0) {
            try {
                Constructor<?> cls = clazz.getDeclaredConstructor(constructorArgs);
                obj = cls.newInstance(initargs);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Failed to instantiate object for " + clazz, e);
            }
        } else {
            obj = null;
        }

        try {
            assertEquals(expectedValue, field.get(obj));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError("Failed to get field " + name + " of class " + clazz, e);
        }
    }

    /**
     * Asserts that class contains constructor with parameter types.
     *
     * @param clazz class to test
     * @param args array of argument classes
     */
    static Constructor<?> assertContainsConstructor(final Class<?> clazz, final Class<?>... args) {
        try {
            return clazz.getDeclaredConstructor(args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Constructor with args " + Arrays.toString(args) + " does not exists in class "
                    + clazz.getSimpleName(), e);
        }
    }

    /**
     * Asserts that class contains method with given name, return type and parameter types.
     *
     * @param clazz class to test
     * @param returnType method return type
     * @param name method name
     * @param args array of parameter type classes
     * @return method with given name, return type and parameter types
     */
    static Method assertContainsMethod(final Class<?> clazz, final Class<?> returnType, final String name,
            final Class<?>... args) {
        try {
            Method method = clazz.getDeclaredMethod(name, args);
            assertEquals(returnType, method.getReturnType());
            return method;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method " + name + " with args " + Arrays.toString(args)
                    + " does not exists in class " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Asserts that class contains method with given name and return type.
     *
     * @param clazz class to test
     * @param returnTypeStr name of method return type
     * @param name method name
     * @param loader current class loader
     */
    static void assertContainsMethod(final Class<?> clazz, final String returnTypeStr, final String name,
            final ClassLoader loader) {
        Class<?> returnType;
        try {
            returnType = Class.forName(returnTypeStr, true, loader);
            Method method = clazz.getMethod(name);
            assertEquals(returnType, method.getReturnType());
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Return type of method '" + name + "' not found", e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method " + name + " does not exists in class " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Asserts that class contains hashCode, equals and toString methods.
     *
     * @param clazz class to test
     */
    static void assertContainsDefaultMethods(final Class<?> clazz) {
        assertContainsMethod(clazz, Integer.TYPE, "hashCode");
        assertContainsMethod(clazz, Boolean.TYPE, "equals", Object.class);
        assertContainsMethod(clazz, String.class, "toString");
    }

    /**
     * Asserts that constructor contains check for illegal argument.
     *
     * @param constructor constructor to invoke
     * @param errorMsg expected error message
     * @param args constructor arguments
     */
    static void assertContainsRestrictionCheck(final Constructor<?> constructor, final String errorMsg,
            final Object... args) {
        final var cause = assertThrows(InvocationTargetException.class, () -> constructor.newInstance(args)).getCause();
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertEquals(errorMsg, cause.getMessage());
    }

    /**
     * Asserts that method contains check for illegal argument.
     *
     * @param obj object to test (can be null, if method is static)
     * @param method method to invoke
     * @param errorMsg expected error message
     * @param args constructor arguments
     */
    static void assertContainsRestrictionCheck(final Object obj, final Method method, final String errorMsg,
            final Object... args) {
        final var cause = assertThrows(InvocationTargetException.class, () -> method.invoke(obj, args)).getCause();
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertEquals(errorMsg, cause.getMessage());
    }

    /**
     * Asserts that class implements given interface.
     *
     * @param clazz source to test
     * @param ifc expected interface
     */
    static void assertImplementsIfc(final Class<?> clazz, final Class<?> ifc) {
        final var ifcsList = Arrays.asList(clazz.getInterfaces());
        if (!ifcsList.contains(ifc)) {
            throw new AssertionError(clazz + " should implement " + ifc);
        }
    }

    /**
     * Test if interface generated from augment extends Augmentation interface with correct generic type.
     *
     * @param clazz interface generated from augment
     * @param genericTypeName fully qualified name of expected parameter type
     */
    static void testAugmentation(final Class<?> clazz, final String genericTypeName) {
        assertImplementsParameterizedIfc(clazz, AUGMENTATION, genericTypeName);
    }

    /**
     * Asserts that class implements interface with given name and generic type parameter.
     *
     * @param clazz class to test
     * @param ifcName name of interface
     * @param genericTypeName name of generic type
     */
    static void assertImplementsParameterizedIfc(final Class<?> clazz, final String ifcName,
            final String genericTypeName) {
        ParameterizedType ifcType = null;
        for (var ifc : clazz.getGenericInterfaces()) {
            if (ifc instanceof ParameterizedType pt && ifcName.equals(pt.getRawType().toString())) {
                ifcType = pt;
            }
        }
        assertNotNull(ifcType);

        Type[] typeArg = ifcType.getActualTypeArguments();
        assertEquals(1, typeArg.length);
        Type typeArgument = typeArg[0];
        assertInstanceOf(Class.class, typeArgument);
        Class<?> argClass = (Class<?>) typeArgument;
        assertEquals(genericTypeName, argClass.getName());
        assertTrue(argClass.isInterface());
    }

    /**
     * Test if source code is compilable.
     *
     * @param sourcesOutputDir directory containing source files
     * @param compiledOutputDir compiler output directory
     */
    static void testCompilation(final Path sourcesOutputDir, final Path compiledOutputDir) {
        final var collector = new DiagnosticCollector<JavaFileObject>();
        final var compiler = ToolProvider.getSystemJavaCompiler();
        final var task = compiler.getTask(null, null, collector, List.of(
            "-proc:none",
            "--source-path", sourcesOutputDir.toAbsolutePath().toString(),
            "-d", compiledOutputDir.toAbsolutePath().toString()), null, null);

        if (!task.call()) {
            final var diags = collector.getDiagnostics();
            final var len = diags.size();
            final var sb = new StringBuilder().append("Compilation failed with ").append(len).append(" messages");
            if (len > 0) {
                sb.append(':');
                for (var diag : diags) {
                    sb.append("\n\n").append(diag);
                }
            }
            throw new AssertionError(sb.toString());
        }
    }

    /**
     * Asserts that directory contains exactly given count of files.
     *
     * @param dir
     *            directory to test
     * @param count
     *            expected count of files in directory
     */
    static void assertFilesCount(final Path dir, final int count) {
        try (var files = Files.list(dir)) {
            assertEquals(count, files.count(), "Unexpected count of generated files");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    static void deleteTestDir(final Path file) throws IOException {
        if (Files.isDirectory(file)) {
            try (var paths = Files.walk(file)) {
                paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } else {
            Files.delete(file);
        }
    }

    static void assertRegularFile(final Path parent, final String name) {
        assertTrue(Files.isRegularFile(parent.resolve(name)));
    }
}
