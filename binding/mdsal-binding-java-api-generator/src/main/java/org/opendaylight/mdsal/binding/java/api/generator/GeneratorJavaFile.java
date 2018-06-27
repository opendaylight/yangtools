/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.base.Preconditions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

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
    private final Collection<? extends Type> types;

    /**
     * BuildContext used for instantiating files
     */
    private final BuildContext buildContext;

    /**
     * Creates instance of this class with the set of <code>types</code> for
     * which the JAVA code is generated.
     *
     * The instances of concrete JAVA code generator are created.
     *
     * @param buildContext
     *            build context to use for accessing files
     * @param types
     *            set of types for which JAVA code should be generated
     */
    public GeneratorJavaFile(final BuildContext buildContext, final Collection<? extends Type> types) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
        this.types = Preconditions.checkNotNull(types);
        generators.add(new InterfaceGenerator());
        generators.add(new TOGenerator());
        generators.add(new EnumGenerator());
        generators.add(new BuilderGenerator());
    }

    /**
     * Creates instance of this class with the set of <code>types</code> for
     * which the JAVA code is generated. Generator instantiated this way uses
     * the default build context, e.g. it will re-generate any and all files.
     *
     * The instances of concrete JAVA code generator are created.
     *
     * @param types
     *            set of types for which JAVA code should be generated
     */
    public GeneratorJavaFile(final Collection<? extends Type> types) {
        this(new DefaultBuildContext(), types);
    }

    /**
     * Generates list of files with JAVA source code. Only the suitable code
     * generator is used to generate the source code for the concrete type.
     *
     * @param generatedSourcesDirectory
     *            directory to which the output source codes should be generated
     * @return list of output files
     * @throws IOException
     *             if the error during writing to the file occurs
     */
    public List<File> generateToFile(final File generatedSourcesDirectory) throws IOException {
        return generateToFile(generatedSourcesDirectory, generatedSourcesDirectory);
    }

    public List<File> generateToFile(final File generatedSourcesDirectory, final File persistenSourcesDirectory)
            throws IOException {
        final List<File> result = new ArrayList<>();
        for (Type type : types) {
            if (type != null) {
                for (CodeGenerator generator : generators) {
                    File generatedJavaFile = null;
                    if (type instanceof GeneratedTransferObject
                            && ((GeneratedTransferObject) type).isUnionTypeBuilder()) {
                        File packageDir = packageToDirectory(persistenSourcesDirectory, type.getPackageName());
                        File file = new File(packageDir, generator.getUnitName(type) + ".java");
                        if (!file.exists()) {
                            generatedJavaFile = generateTypeToJavaFile(persistenSourcesDirectory, type, generator);
                        }
                    } else {
                        generatedJavaFile = generateTypeToJavaFile(generatedSourcesDirectory, type, generator);
                    }
                    if (generatedJavaFile != null) {
                        result.add(generatedJavaFile);
                    }
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
     *             if the error during writing to the file occurs
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
            final String generatedCode = generator.generate(type);
            if (generatedCode.isEmpty()) {
                throw new IllegalStateException("Generated code should not be empty!");
            }
            final File file = new File(packageDir, generator.getUnitName(type) + ".java");

            if (file.exists()) {
                LOG.warn(
                        "Naming conflict for type '{}': file with same name already exists and will not be generated.",
                        type.getFullyQualifiedName());
                return null;
            }

            try (final OutputStream stream = buildContext.newFileOutputStream(file)) {
                try (final Writer fw = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                    try (final BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write(generatedCode);
                    }
                } catch (IOException e) {
                    LOG.error("Failed to write generate output into {}", file.getPath(), e);
                    throw e;
                }
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
    public static File packageToDirectory(final File parentDirectory, final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package Name cannot be NULL!");
        }

        return new File(parentDirectory, packageName.replace('.', File.separatorChar));
    }
}
