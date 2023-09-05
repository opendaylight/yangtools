/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class CompilationTestUtils {
    public static final String FS = File.separator;
    static final String BASE_PKG = "org.opendaylight.yang.gen.v1";

    static final String TEST_PATH = "target" + FS + "test";
    static final File TEST_DIR = new File(TEST_PATH);

    static final String GENERATOR_OUTPUT_PATH = TEST_PATH + FS + "src";
    static final File GENERATOR_OUTPUT_DIR = new File(GENERATOR_OUTPUT_PATH);
    private static final String COMPILER_OUTPUT_PATH = TEST_PATH + FS + "bin";
    static final File COMPILER_OUTPUT_DIR = new File(COMPILER_OUTPUT_PATH);

    static final String AUGMENTATION = "interface org.opendaylight.yangtools.yang.binding.Augmentation";
    static final String BASE_PATH = "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS + "v1";
    static final String BASE_SVC_PATH = "org" + FS + "opendaylight" + FS + "yang" + FS + "svc" + FS + "v1";
    static final String NS_TEST = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "test" + FS + "rev131008";
    static final String NS_SVC_TEST = BASE_SVC_PATH + FS + "urn" + FS + "opendaylight" + FS + "test" + FS + "rev131008";
    static final String NS_FOO = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "foo" + FS + "rev131008";
    static final String NS_SVC_FOO = BASE_SVC_PATH + FS + "urn" + FS + "opendaylight" + FS + "foo" + FS + "rev131008";
    static final String NS_BAR = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "bar" + FS + "rev131008";
    static final String NS_SVC_BAR = BASE_SVC_PATH + FS + "urn" + FS + "opendaylight" + FS + "bar" + FS + "rev131008";
    static final String NS_BAZ = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "baz" + FS + "rev131008";
    static final String NS_SVC_BAZ = BASE_SVC_PATH + FS + "urn" + FS + "opendaylight" + FS + "baz" + FS + "rev131008";
    static final String NS_BUG5882 = BASE_PATH + FS + "urn" + FS + "yang" + FS + "foo" + FS + "rev160102";

    private CompilationTestUtils() {

    }

    static File compilerOutput(final String name) {
        return createDirectory(CompilationTestUtils.COMPILER_OUTPUT_PATH + CompilationTestUtils.FS + name);
    }

    static File generatorOutput(final String name) {
        return createDirectory(CompilationTestUtils.GENERATOR_OUTPUT_PATH + CompilationTestUtils.FS + name);
    }

    private static File createDirectory(final String path) {
        final File ret = new File(path);
        checkState(ret.mkdir(), "Failed to create test directory %s", path);
        return ret;
    }

    /**
     * Method to clean resources. It is manually called at the end of each test instead of marking it with @After
     * annotation to prevent removing generated code if test fails.
     */
    static void cleanUp(final File... resourceDirs) {
        for (File resourceDir : resourceDirs) {
            if (resourceDir.exists()) {
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
        Field field = assertContainsField(clazz, name, returnType);
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
            final Object... args) throws ReflectiveOperationException {
        final var cause = assertThrows(InvocationTargetException.class, () -> constructor.newInstance(args)).getCause();
        assertThat(cause, instanceOf(IllegalArgumentException.class));
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
            final Object... args) throws ReflectiveOperationException {
        final var cause = assertThrows(InvocationTargetException.class, () -> method.invoke(obj, args)).getCause();
        assertThat(cause, instanceOf(IllegalArgumentException.class));
        assertEquals(errorMsg, cause.getMessage());
    }

    /**
     * Asserts that class implements given interface.
     *
     * @param clazz source to test
     * @param ifc expected interface
     */
    static void assertImplementsIfc(final Class<?> clazz, final Class<?> ifc) {
        List<Class<?>> ifcsList = Arrays.asList(clazz.getInterfaces());
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
        for (Type ifc : clazz.getGenericInterfaces()) {
            if (ifc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) ifc;
                if (ifcName.equals(pt.getRawType().toString())) {
                    ifcType = pt;
                }
            }
        }
        assertNotNull(ifcType);

        Type[] typeArg = ifcType.getActualTypeArguments();
        assertEquals(1, typeArg.length);
        Type typeArgument = typeArg[0];
        assertThat(typeArgument, instanceOf(Class.class));
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
    static void testCompilation(final File sourcesOutputDir, final File compiledOutputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());

        List<Diagnostic<?>> diags = new ArrayList<>();
        boolean compiled = compiler.getTask(null, null, diags::add, options, null, compilationUnits).call();
        if (!compiled) {
            fail("Compilation failed with " + diags);
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
    static void assertFilesCount(final File dir, final int count) {
        File[] dirContent = dir.listFiles();
        if (dirContent == null) {
            throw new AssertionError("File " + dir + " doesn't exists or it's not a directory");
        }

        assertEquals("Unexpected count of generated files", count, dirContent.length);
    }

    /**
     * Search recursively given directory for *.java files.
     *
     * @param directory directory to search
     * @return List of java files found
     */
    private static List<File> getJavaFiles(final File directory) {
        List<File> result = new ArrayList<>();
        File[] filesToRead = directory.listFiles();
        if (filesToRead != null) {
            for (File file : filesToRead) {
                if (file.isDirectory()) {
                    result.addAll(getJavaFiles(file));
                } else {
                    String absPath = file.getAbsolutePath();
                    if (absPath.endsWith(".java")) {
                        result.add(file);
                    }
                }
            }
        }
        return result;
    }

    static List<File> getSourceFiles(final String path) throws FileNotFoundException, URISyntaxException {
        final URI resPath = BaseCompilationTest.class.getResource(path).toURI();
        final File sourcesDir = new File(resPath);
        if (!sourcesDir.exists()) {
            throw new FileNotFoundException("Testing files were not found(" + sourcesDir.getName() + ")");
        }

        final List<File> sourceFiles = new ArrayList<>();
        final File[] fileArray = sourcesDir.listFiles();
        if (fileArray == null) {
            throw new IllegalArgumentException("Unable to locate files in " + sourcesDir);
        }
        sourceFiles.addAll(Arrays.asList(fileArray));
        return sourceFiles;
    }

    static void deleteTestDir(final File file) {
        if (file.isDirectory()) {
            File[] filesToDelete = file.listFiles();
            if (filesToDelete != null) {
                for (File f : filesToDelete) {
                    deleteTestDir(f);
                }
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Failed to clean up after test");
        }
    }
}
