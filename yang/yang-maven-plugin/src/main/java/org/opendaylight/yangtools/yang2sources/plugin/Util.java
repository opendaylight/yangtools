/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Util {

    /**
     * It isn't desirable to create instances of this class.
     */
    private Util() {
    }

    static final String YANG_SUFFIX = "yang";

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    static Collection<File> listFiles(final File root, final Collection<File> excludedFiles)
            throws FileNotFoundException {
        if (!root.exists()) {
            LOG.warn("{} YANG source directory {} not found. No code will be generated.", YangToSourcesProcessor
                    .LOG_PREFIX, root.toString());

            return Collections.emptyList();
        }
        Collection<File> result = new ArrayList<>();
        Collection<File> yangFiles = FileUtils.listFiles(root, new String[] { YANG_SUFFIX }, true);
        for (File f : yangFiles) {
            if (excludedFiles.contains(f)) {
                LOG.info("{} {} file excluded {}", YangToSourcesProcessor.LOG_PREFIX, Util.YANG_SUFFIX.toUpperCase(),
                        f);
            } else {
                result.add(f);
            }
        }

        return result;
    }

    static List<File> getClassPath(final MavenProject project) {
        List<File> dependencies = new ArrayList<>();
        for (Artifact element : project.getArtifacts()) {
            File asFile = element.getFile();
            if (isJar(asFile) || asFile.isDirectory()) {
                dependencies.add(asFile);
            }
        }
        return dependencies;
    }

    /**
     * Read current project dependencies and check if it don't grab incorrect
     * artifacts versions which could be in conflict with plugin dependencies.
     *
     * @param project
     *            current project
     * @param repoSystem
     *            repository system
     * @param localRepo
     *            local repository
     * @param remoteRepos
     *            remote repositories
     */
    static void checkClasspath(final MavenProject project, final RepositorySystem repoSystem,
            final ArtifactRepository localRepo, final List<ArtifactRepository> remoteRepos) {
        Plugin plugin = project.getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        if (plugin == null) {
            LOG.warn("{} {} not found, dependencies version check skipped", YangToSourcesProcessor.LOG_PREFIX,
                    YangToSourcesMojo.PLUGIN_NAME);
        } else {
            Map<Artifact, Collection<Artifact>> pluginDependencies = new HashMap<>();
            getPluginTransitiveDependencies(plugin, pluginDependencies, repoSystem, localRepo, remoteRepos);

            Set<Artifact> projectDependencies = project.getDependencyArtifacts();
            for (Map.Entry<Artifact, Collection<Artifact>> entry : pluginDependencies.entrySet()) {
                checkArtifact(entry.getKey(), projectDependencies);
                for (Artifact dependency : entry.getValue()) {
                    checkArtifact(dependency, projectDependencies);
                }
            }
        }
    }

    /**
     * Read transitive dependencies of given plugin and store them in map.
     *
     * @param plugin
     *            plugin to read
     * @param map
     *            map, where founded transitive dependencies will be stored
     * @param repoSystem
     *            repository system
     * @param localRepository
     *            local repository
     * @param remoteRepos
     *            list of remote repositories
     */
    private static void getPluginTransitiveDependencies(final Plugin plugin,
            final Map<Artifact, Collection<Artifact>> map, final RepositorySystem repoSystem,
            final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepos) {

        List<Dependency> pluginDependencies = plugin.getDependencies();
        for (Dependency dep : pluginDependencies) {
            Artifact artifact = repoSystem.createDependencyArtifact(dep);

            ArtifactResolutionRequest request = new ArtifactResolutionRequest();
            request.setArtifact(artifact);
            request.setResolveTransitively(true);
            request.setLocalRepository(localRepository);
            request.setRemoteRepositories(remoteRepos);

            ArtifactResolutionResult result = repoSystem.resolve(request);
            Set<Artifact> pluginDependencyDependencies = result.getArtifacts();
            map.put(artifact, pluginDependencyDependencies);
        }
    }

    /**
     * Check artifact against collection of dependencies. If collection contains
     * artifact with same groupId and artifactId, but different version, logs a
     * warning.
     *
     * @param artifact
     *            artifact to check
     * @param dependencies
     *            collection of dependencies
     */
    private static void checkArtifact(final Artifact artifact, final Collection<Artifact> dependencies) {
        for (org.apache.maven.artifact.Artifact d : dependencies) {
            if (artifact.getGroupId().equals(d.getGroupId()) && artifact.getArtifactId().equals(d.getArtifactId())) {
                if (!(artifact.getVersion().equals(d.getVersion()))) {
                    LOG.warn("{} Dependency resolution conflict:", YangToSourcesProcessor.LOG_PREFIX);
                    LOG.warn("{} '{}' dependency [{}] has different version than one declared in current project [{}]"
                            + ". It is recommended to fix this problem because it may cause compilation errors.",
                            YangToSourcesProcessor.LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME, artifact, d);
                }
            }
        }
    }

    private static final String JAR_SUFFIX = ".jar";

    private static boolean isJar(final File element) {
        return (element.isFile() && element.getName().endsWith(JAR_SUFFIX));
    }

    static final class YangsInZipsResult implements Closeable {
        private final List<YangSourceFromDependency> yangStreams;
        private final List<Closeable> zipInputStreams;

        private YangsInZipsResult(final List<YangSourceFromDependency> yangStreams,
                final List<Closeable> zipInputStreams) {
            this.yangStreams = yangStreams;
            this.zipInputStreams = zipInputStreams;
        }

        @Override
        public void close() throws IOException {
            for (Closeable is : zipInputStreams) {
                is.close();
            }
        }

        public List<YangSourceFromDependency> getYangStreams() {
            return this.yangStreams;
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    static YangsInZipsResult findYangFilesInDependenciesAsStream(final MavenProject project)
            throws MojoFailureException {
        List<YangSourceFromDependency> yangsFromDependencies = new ArrayList<>();
        List<Closeable> zips = new ArrayList<>();
        try {
            List<File> filesOnCp = Util.getClassPath(project);
            LOG.info("{} Searching for yang files in following dependencies: {}", YangToSourcesProcessor.LOG_PREFIX,
                    filesOnCp);

            for (File file : filesOnCp) {
                List<String> foundFilesForReporting = new ArrayList<>();
                // is it jar file or directory?
                if (file.isDirectory()) {
                    //FIXME: code duplicate
                    File yangDir = new File(file, YangToSourcesProcessor.META_INF_YANG_STRING);
                    if (yangDir.exists() && yangDir.isDirectory()) {
                        File[] yangFiles = yangDir.listFiles(
                            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile());
                        for (final File yangFile : yangFiles) {
                            yangsFromDependencies.add(new YangSourceFromFile(yangFile));
                        }
                    }

                } else {
                    ZipFile zip = new ZipFile(file);
                    zips.add(zip);

                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if (entryName.startsWith(YangToSourcesProcessor.META_INF_YANG_STRING_JAR)
                                && !entry.isDirectory() && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
                            foundFilesForReporting.add(entryName);
                            yangsFromDependencies.add(new YangSourceInZipFile(zip, entry));
                        }
                    }
                }
                if (foundFilesForReporting.size() > 0) {
                    LOG.info("{} Found {} yang files in {}: {}", YangToSourcesProcessor.LOG_PREFIX,
                            foundFilesForReporting.size(), file, foundFilesForReporting);
                }

            }
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
        return new YangsInZipsResult(yangsFromDependencies, zips);
    }

    /**
     * Find all dependencies which contains yang sources.
     * Returns collection of YANG files and Zip files which contains YANG files.
     *
     */
    //  FIXME: Rename to what class is actually doing.
    @SuppressWarnings("checkstyle:illegalCatch")
    static Collection<File> findYangFilesInDependencies(final MavenProject project) throws MojoFailureException {
        final List<File> yangsFilesFromDependencies = new ArrayList<>();

        List<File> filesOnCp;
        try {
            filesOnCp = Util.getClassPath(project);
        } catch (Exception e) {
            throw new MojoFailureException("Failed to scan for YANG files in dependencies", e);
        }
        LOG.info("{} Searching for yang files in following dependencies: {}", YangToSourcesProcessor.LOG_PREFIX,
            filesOnCp);

        for (File file : filesOnCp) {
            try {
                // is it jar file or directory?
                if (file.isDirectory()) {
                    //FIXME: code duplicate
                    File yangDir = new File(file, YangToSourcesProcessor.META_INF_YANG_STRING);
                    if (yangDir.exists() && yangDir.isDirectory()) {
                        File[] yangFiles = yangDir.listFiles(
                            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile());

                        yangsFilesFromDependencies.addAll(Arrays.asList(yangFiles));
                    }
                } else {
                    try (ZipFile zip = new ZipFile(file)) {

                        final Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            String entryName = entry.getName();

                            if (entryName.startsWith(YangToSourcesProcessor.META_INF_YANG_STRING_JAR)
                                    && !entry.isDirectory() && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
                                LOG.debug("{} Found a YANG file in {}: {}", YangToSourcesProcessor.LOG_PREFIX, file,
                                        entryName);
                                yangsFilesFromDependencies.add(file);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new MojoFailureException("Failed to scan for YANG files in dependency: " + file.toString(), e);
            }
        }
        return yangsFilesFromDependencies;
    }

    static final class ContextHolder {
        private static final Splitter SEP_SPLITTER = Splitter.on(File.separator);

        private final SchemaContext context;
        private final Set<Module> yangModules;
        private final Set<Module> yangFiles;

        ContextHolder(final SchemaContext context, final Set<Module> yangModules, final Set<Module> yangFiles) {
            this.context = context;
            this.yangModules = yangModules;
            this.yangFiles = yangFiles;
        }

        SchemaContext getContext() {
            return context;
        }

        Set<Module> getYangModules() {
            return yangModules;
        }

        Set<Module> getYangFiles() {
            return yangFiles;
        }

        Optional<String> moduleToResourcePath(final Module mod) {
            if (!yangFiles.contains(mod)) {
                return Optional.empty();
            }

            return Optional.of("/" + YangToSourcesProcessor.META_INF_YANG_STRING_JAR + "/"
                    + Iterables.getLast(SEP_SPLITTER.split(mod.getModuleSourcePath())));
        }
    }
}
