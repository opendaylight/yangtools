/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generate sources from yang files using user provided set of
 * {@link BasicCodeGenerator}s. Steps of this process:
 * <ol>
 * <li>List yang files from {@link #yangFilesRootDir}</li>
 * <li>Process yang files using Yang Parser</li>
 * <li>For each {@link BasicCodeGenerator} from {@link #codeGenerators}:
 * <ol>
 * <li>Instantiate using default constructor</li>
 * <li>Call {@link BasicCodeGenerator#generateSources(SchemaContext, File, Set)}</li>
 * </ol></li>
 * </ol>
 */
@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public final class YangToSourcesMojo extends AbstractMojo {
    public static final String PLUGIN_NAME = "org.opendaylight.yangtools:yang-maven-plugin";

    /**
     * Classes implementing {@link BasicCodeGenerator} interface. An instance will be
     * created out of every class using default constructor. Method {@link
     * CodeGenerator#generateSources(SchemaContext, File, Set<String>
     * yangModulesNames)} will be called on every instance.
     */
    @Parameter(required = false)
    private CodeGeneratorArg[] codeGenerators;

    /**
     * Source directory that will be recursively searched for yang files (ending
     * with .yang suffix).
     */
    @Parameter(required = false)
    // defaults to ${basedir}/src/main/yang
    private String yangFilesRootDir;

    @Parameter(required = false)
    private String[] excludeFiles;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "inspectDependencies", required = true, readonly = true)
    private boolean inspectDependencies;

    @Component
    private BuildContext buildContext;

    private YangToSourcesProcessor yangToSourcesProcessor;

    @Component
    private RepositorySystem repoSystem;

    @Parameter( readonly = true, defaultValue = "${localRepository}" )
    private ArtifactRepository localRepository;

    @Parameter( readonly = true, defaultValue = "${project.remoteArtifactRepositories}" )
    private List<ArtifactRepository> remoteRepos;


    public YangToSourcesMojo() {
    }

    public void setProject(final MavenProject project) {
        this.project = project;
    }

    @VisibleForTesting
    YangToSourcesMojo(final YangToSourcesProcessor processor) {
        this.yangToSourcesProcessor = processor;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.checkClasspath(project, repoSystem, localRepository, remoteRepos, getLog());

        if (yangToSourcesProcessor == null) {
            List<CodeGeneratorArg> codeGeneratorArgs = processCodeGenerators(codeGenerators);

            // defaults to ${basedir}/src/main/yang
            File yangFilesRootFile = processYangFilesRootDir(yangFilesRootDir, project.getBasedir());
            File[] excludedFiles = processExcludeFiles(excludeFiles, yangFilesRootFile);

            yangToSourcesProcessor = new YangToSourcesProcessor(buildContext, getLog(), yangFilesRootFile,
                    excludedFiles, codeGeneratorArgs, project, inspectDependencies);
        }
        yangToSourcesProcessor.execute();
    }

    private static List<CodeGeneratorArg> processCodeGenerators(final CodeGeneratorArg[] codeGenerators) {
        List<CodeGeneratorArg> codeGeneratorArgs;
        if (codeGenerators == null) {
            codeGeneratorArgs = Collections.emptyList();
        } else {
            codeGeneratorArgs = Arrays.asList(codeGenerators);
        }
        return codeGeneratorArgs;
    }

    private static File processYangFilesRootDir(final String yangFilesRootDir, final File baseDir) {
        File yangFilesRootFile;
        if (yangFilesRootDir == null) {
            yangFilesRootFile = new File(baseDir, "src" + File.separator + "main" + File.separator + "yang");
        } else {
            File file = new File(yangFilesRootDir);
            if (file.isAbsolute()) {
                yangFilesRootFile = file;
            } else {
                yangFilesRootFile = new File(baseDir, file.getPath());
            }
        }
        return yangFilesRootFile;
    }

    private static File[] processExcludeFiles(final String[] excludeFiles, final File baseDir) {
        if (excludeFiles == null) {
            return new File[] {};
        }
        File[] result = new File[excludeFiles.length];
        for (int i = 0; i < excludeFiles.length; i++) {
            result[i] = new File(baseDir, excludeFiles[i]);
        }

        return result;
    }

}
