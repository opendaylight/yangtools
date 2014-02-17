/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.maven.sal.api.gen.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.sal.java.api.generator.YangModuleInfoTemplate;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.CodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public final class CodeGeneratorImpl implements CodeGenerator, BuildContextAware {
    private static final String FS = File.separator;
    private BuildContext buildContext;
    private File projectBaseDir;
    private Map<String, String> additionalConfig;

    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratorImpl.class);
    private MavenProject mavenProject;
    private File resourceBaseDir;

    @Override
    public Collection<File> generateSources(final SchemaContext context, final File outputDir,
            final Set<Module> yangModules) throws IOException {
        final File outputBaseDir;

        outputBaseDir = outputDir == null ? getDefaultOutputBaseDir() : outputDir;

        final BindingGenerator bindingGenerator = new BindingGeneratorImpl();
        final List<Type> types = bindingGenerator.generateTypes(context, yangModules);
        final GeneratorJavaFile generator = new GeneratorJavaFile(buildContext, new HashSet<>(types));

        File persistentSourcesDir = null;
        if (additionalConfig != null) {
            String persistenSourcesPath = additionalConfig.get("persistentSourcesDir");
            if (persistenSourcesPath != null) {
                persistentSourcesDir = new File(persistenSourcesPath);
            }
        }
        if (persistentSourcesDir == null) {
            persistentSourcesDir = new File(projectBaseDir, "src" + FS + "main" + FS + "java");
        }

        List<File> result = generator.generateToFile(outputBaseDir, persistentSourcesDir);

        result.addAll(generateModuleInfos(outputBaseDir, yangModules, context));
        return result;
    }

    private Collection<? extends File> generateModuleInfos(File outputBaseDir, Set<Module> yangModules,
            SchemaContext context) {
        Builder<File> result = ImmutableSet.builder();
        Builder<String> bindingProviders = ImmutableSet.builder();
        for (Module module : yangModules) {
            result.addAll(generateYangModuleInfo(outputBaseDir, module, context, bindingProviders));
        }

        result.add(writeMetaInfServices(resourceBaseDir, YangModelBindingProvider.class, bindingProviders.build()));
        return result.build();
    }

    private File writeMetaInfServices(File outputBaseDir, Class<YangModelBindingProvider> serviceClass,
            ImmutableSet<String> services) {
        File metainfServicesFolder = new File(outputBaseDir, "META-INF" + File.separator + "services");
        metainfServicesFolder.mkdirs();
        File serviceFile = new File(metainfServicesFolder, serviceClass.getName());

        String src = Joiner.on('\n').join(services);

        return writeFile(serviceFile, src);
    }

    public static final String DEFAULT_OUTPUT_BASE_DIR_PATH = "target" + File.separator + "generated-sources"
            + File.separator + "maven-sal-api-gen";

    private File getDefaultOutputBaseDir() {
        File outputBaseDir;
        outputBaseDir = new File(DEFAULT_OUTPUT_BASE_DIR_PATH);
        setOutputBaseDirAsSourceFolder(outputBaseDir, mavenProject);
        logger.debug("Adding " + outputBaseDir.getPath() + " as compile source root");
        return outputBaseDir;
    }

    private static void setOutputBaseDirAsSourceFolder(File outputBaseDir, MavenProject mavenProject) {
        Preconditions.checkNotNull(mavenProject, "Maven project needs to be set in this phase");
        mavenProject.addCompileSourceRoot(outputBaseDir.getPath());
    }

    @Override
    public void setLog(Log log) {
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
        this.additionalConfig = additionalConfiguration;
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
        this.resourceBaseDir = resourceBaseDir;
    }

    @Override
    public void setMavenProject(MavenProject project) {
        this.mavenProject = project;
        this.projectBaseDir = project.getBasedir();
    }

    @Override
    public void setBuildContext(BuildContext buildContext) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
    }

    private Set<File> generateYangModuleInfo(File outputBaseDir, Module module, SchemaContext ctx,
            Builder<String> providerSourceSet) {
        Builder<File> generatedFiles = ImmutableSet.<File> builder();

        final YangModuleInfoTemplate template = new YangModuleInfoTemplate(module, ctx);
        String moduleInfoSource = template.generate();
        if (moduleInfoSource.isEmpty()) {
            throw new IllegalStateException("Generated code should not be empty!");
        }
        String providerSource = template.generateModelProvider();

        final File packageDir = GeneratorJavaFile.packageToDirectory(outputBaseDir,
                BindingGeneratorUtil.moduleNamespaceToPackageName(module));

        generatedFiles.add(writeJavaSource(packageDir, BindingMapping.MODULE_INFO_CLASS_NAME, moduleInfoSource));
        generatedFiles
                .add(writeJavaSource(packageDir, BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME, providerSource));
        providerSourceSet.add(template.getModelBindingProviderName());

        return generatedFiles.build();

    }

    private File writeJavaSource(File packageDir, String className, String source) {
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
        final File file = new File(packageDir, className + ".java");
        writeFile(file, source);
        return file;
    }

    private File writeFile(File file, String source) {
        try (final OutputStream stream = buildContext.newFileOutputStream(file)) {
            try (final Writer fw = new OutputStreamWriter(stream)) {
                try (final BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(source);
                }
            } catch (Exception e) {
                logger.error("Could not write file: {}",file,e);
            }
        } catch (Exception e) {
            logger.error("Could not create file: {}",file,e);
        }
        return file;
    }

}
