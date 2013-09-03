/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates files with JAVA source codes for every specified type.
 * 
 */
public final class GeneratorJavaFile {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorJavaFile.class);

    /**
     * List of <code>CodeGenerator</code> instances.
     */
    private final List<CodeGenerator> generators = new ArrayList<>();

    /**
     * Set of <code>Type</code> instances for which the JAVA code is generated.
     */
    private final Set<? extends Type> types;

    /**
     * Creates instance of this class with the set of <code>types</code> for
     * which the JAVA code is generated.
     * 
     * The instances of concrete JAVA code generator are created.
     * 
     * @param types
     *            set of types for which JAVA code should be generated
     */
    public GeneratorJavaFile(final Set<? extends Type> types) {
        this.types = types;
        generators.add(new InterfaceGenerator());
        generators.add(new TOGenerator());
        generators.add(new EnumGenerator());
        generators.add(new BuilderGenerator());
    }

    /**
     * Generates list of files with JAVA source code. Only the suitable code
     * generator is used to generate the source code for the concrete type.
     * 
     * @param parentDirectory
     *            directory to which the output source codes should be generated
     * @return list of output files
     * @throws IOException
     *             if the error during writting to the file occures
     */
    public List<File> generateToFile(final File parentDirectory) throws IOException {
        final List<File> result = new ArrayList<>();
        for (Type type : types) {
            for (CodeGenerator generator : generators) {
                File generatedJavaFile = generateTypeToJavaFile(parentDirectory, type, generator);
                if (generatedJavaFile != null) {
                    result.add(generatedJavaFile);
                }
            }
        }
        return result;
    }

    /**
     * Generates <code>File</code> for <code>type</code>. All files are stored
     * to subfolders of base directory <code>parentDir</code>. Subdirectories
     * are generated according to packages to which the type belongs (e. g. if
     * type belongs to the package <i>org.pcg</i> then in <code>parentDir</code>
     * is created directory <i>org</i> which contains <i>pcg</i>).
     * 
     * @param parentDir
     *            directory where should be the new file generated
     * @param type
     *            JAVA <code>Type</code> for which should be JAVA source code
     *            generated
     * @param generator
     *            code generator which is used for generating of the source code
     * @return file which contains JAVA source code
     * @throws IOException
     *             if the error during writting to the file occures
     * @throws IllegalArgumentException
     *             if <code>type</code> equals <code>null</code>
     * @throws IllegalStateException
     *             if string with generated code is empty
     */
    private File generateTypeToJavaFile(final File parentDir, final Type type, final CodeGenerator generator)
            throws IOException {
        if (parentDir == null) {
            LOG.warn("Parent Directory not specified, files will be generated "
                    + "accordingly to generated Type package path.");
        }
        if (type == null) {
            LOG.error("Cannot generate Type into Java File because " + "Generated Type is NULL!");
            throw new IllegalArgumentException("Generated Type Cannot be NULL!");
        }
        if (generator == null) {
            LOG.error("Cannot generate Type into Java File because " + "Code Generator instance is NULL!");
            throw new IllegalArgumentException("Code Generator Cannot be NULL!");
        }
        final File packageDir = packageToDirectory(parentDir, type.getPackageName());

        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }

        if (generator.isAcceptable(type)) {
            String generatedCode = generator.generate(type);
            if (generatedCode.isEmpty()) {
                throw new IllegalStateException("Generated code should not be empty!");
            }
            final File file = new File(packageDir, generator.getUnitName(type) + ".java");
            try (final FileWriter fw = new FileWriter(file)) {
                file.createNewFile();
                try (final BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(generatedCode);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
                throw new IOException(e);
            }
            return file;
        }
        return null;
    }

    /**
     * Creates the package directory path as concatenation of
     * <code>parentDirectory</code> and parsed <code>packageName</code>. The
     * parsing of <code>packageName</code> is realized as replacement of the
     * package name dots with the file system separator.
     * 
     * @param parentDirectory
     *            <code>File</code> object with reference to parent directory
     * @param packageName
     *            string with the name of the package
     * @return <code>File</code> object which refers to the new directory for
     *         package <code>packageName</code>
     */
    private File packageToDirectory(final File parentDirectory, final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package Name cannot be NULL!");
        }

        final String[] subDirNames = packageName.split("\\.");
        final StringBuilder dirPathBuilder = new StringBuilder();
        dirPathBuilder.append(subDirNames[0]);
        for (int i = 1; i < subDirNames.length; ++i) {
            dirPathBuilder.append(File.separator);
            dirPathBuilder.append(subDirNames[i]);
        }
        return new File(parentDirectory, dirPathBuilder.toString());
    }
}
