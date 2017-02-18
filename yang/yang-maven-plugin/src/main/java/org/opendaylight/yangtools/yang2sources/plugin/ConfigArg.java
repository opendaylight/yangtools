/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.Map;
import org.apache.maven.project.MavenProject;

/**
 * Base complex configuration arguments
 */
public abstract class ConfigArg {

    private final File outputBaseDir;

    public ConfigArg(final String outputBaseDir) {
        this.outputBaseDir = outputBaseDir == null ? null : new File(outputBaseDir);
    }

    public File getOutputBaseDir(final MavenProject project) {
        if (outputBaseDir.isAbsolute()) {
            return outputBaseDir;
        }

        return new File(project.getBasedir(), outputBaseDir.getPath());
    }

    public void check() {
        Preconditions.checkNotNull(outputBaseDir,
            "outputBaseDir is null. Please provide a valid outputBaseDir value in the pom.xml");
    }

    /**
     * Configuration argument for code generator class and output directory.
     */
    public static final class CodeGeneratorArg extends ConfigArg {
        private static final String TARGET_GENERATED_SOURCES = "target" + File.separator + "generated-sources";
        private static final String CODE_GEN_DEFAULT_RESOURCE_DIR = TARGET_GENERATED_SOURCES + File.separator + "spi";
        public static final String YANG_GENERATED_DIR = TARGET_GENERATED_SOURCES + File.separator + "yang";
        public static final String YANG_SERVICES_GENERATED_DIR = TARGET_GENERATED_SOURCES + File.separator + "spi";


        private String codeGeneratorClass;
        private File resourceBaseDir = new File(CODE_GEN_DEFAULT_RESOURCE_DIR);

        private final Map<String, String> additionalConfiguration = Maps.newHashMap();

        public CodeGeneratorArg() {
            super(null);
        }

        public CodeGeneratorArg(final String codeGeneratorClass) {
            this(codeGeneratorClass, null);
        }

        public CodeGeneratorArg(final String codeGeneratorClass, final String outputBaseDir) {
            super(outputBaseDir);
            this.codeGeneratorClass = codeGeneratorClass;
        }

        public CodeGeneratorArg(final String codeGeneratorClass, final String outputBaseDir, final String resourceBaseDir) {
            super(outputBaseDir);
            this.codeGeneratorClass = codeGeneratorClass;
            this.resourceBaseDir = new File(resourceBaseDir);
        }

        @Override
        public void check() {
            super.check();
            Preconditions.checkNotNull(codeGeneratorClass, "codeGeneratorClass for CodeGenerator cannot be null");
        }

        public String getCodeGeneratorClass() {
            return codeGeneratorClass;
        }

        public File getResourceBaseDir(final MavenProject project) {
            if (resourceBaseDir.isAbsolute()) {
                return resourceBaseDir;
            }

            return new File(project.getBasedir(), resourceBaseDir.getPath());
        }

        public Map<String, String> getAdditionalConfiguration() {
            return additionalConfiguration;
        }
    }
}
