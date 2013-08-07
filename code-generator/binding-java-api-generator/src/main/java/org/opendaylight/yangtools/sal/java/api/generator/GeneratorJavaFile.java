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

public final class GeneratorJavaFile {

    private static final Logger log = LoggerFactory.getLogger(GeneratorJavaFile.class);
    private final List<CodeGenerator> generators = new ArrayList<>();

    private final Set<? extends Type> types;

    public GeneratorJavaFile(final Set<? extends Type> types) {
    	this.types = types;
    	generators.add(new InterfaceGenerator());
    	generators.add(new TOGenerator());
    	generators.add(new EnumGenerator());
    	generators.add(new BuilderGenerator());
    }

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

    private File generateTypeToJavaFile(final File parentDir, final Type type, final CodeGenerator generator)
            throws IOException {
        if (parentDir == null) {
            log.warn("Parent Directory not specified, files will be generated "
                    + "accordingly to generated Type package path.");
        }
        if (type == null) {
            log.error("Cannot generate Type into Java File because " + "Generated Type is NULL!");
            throw new IllegalArgumentException("Generated Type Cannot be NULL!");
        }
        if (generator == null) {
            log.error("Cannot generate Type into Java File because " + "Code Generator instance is NULL!");
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
                log.error(e.getMessage());
                throw new IOException(e.getMessage());
            }
            return file;
        }
        return null;
    }
    
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
