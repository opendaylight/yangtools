/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates files with JAVA source codes for every specified type.
 *
 */
public final class GeneratorJavaFile {
    public enum FileKind {
        /**
         * Transient file. It should be generated in target/generated-sources/ directory or similar.
         */
        TRANSIENT,
        /**
         * Persistent file. It should be generated in src/main/java/ directory or similar.
         */
        PERSISTENT,
    }

    private static final class GeneratorStringSupplier implements Supplier<String> {
        private final CodeGenerator generator;
        private final Type type;

        GeneratorStringSupplier(final CodeGenerator generator, final Type type) {
            this.generator = requireNonNull(generator);
            this.type = requireNonNull(type);
        }

        @Override
        public String get() {
            return generator.generate(type);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("generator", generator).add("type", type).toString();
        }
    }

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
        this.types = Preconditions.checkNotNull(types);
        generators.add(new InterfaceGenerator());
        generators.add(new TOGenerator());
        generators.add(new EnumGenerator());
        generators.add(new BuilderGenerator());
    }

    public Table<FileKind, String, Supplier<String>> generateFileContent(final boolean ignoreDuplicates) {
        final Table<FileKind, String, Supplier<String>> result = HashBasedTable.create();
        for (Type type : types) {
            for (CodeGenerator generator : generators) {
                if (!generator.isAcceptable(type)) {
                    continue;
                }

                final FileKind kind = type instanceof GeneratedTransferObject
                        && ((GeneratedTransferObject) type).isUnionTypeBuilder()
                        ? FileKind.PERSISTENT : FileKind.TRANSIENT;
                final String file = type.getPackageName().replace('.', File.separatorChar)
                        +  File.separator + generator.getUnitName(type) + ".java";

                if (result.contains(kind, file)) {
                    if (ignoreDuplicates) {
                        LOG.warn("Naming conflict for type '{}': file with same name already exists and will not be "
                                + "generated.", type.getFullyQualifiedName());
                        continue;
                    }
                    throw new IllegalStateException("Duplicate " + kind + " file '" + file + "' for "
                            + type.getFullyQualifiedName());
                }

                result.put(kind, file, new GeneratorStringSupplier(generator, type));
            }
        }

        return result;
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
