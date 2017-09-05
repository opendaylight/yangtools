/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.opendaylight.yangtools.yang2sources.plugin.Util.ContextHolder;
import org.opendaylight.yangtools.yang2sources.plugin.Util.YangsInZipsResult;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {

    @Test
    public void getClassPathTest() {
        final MavenProject project = Mockito.mock(MavenProject.class);
        final File file = Mockito.mock(File.class);
        final File file2 = Mockito.mock(File.class);
        final Artifact artifact = Mockito.mock(Artifact.class);
        final Artifact artifact2 = Mockito.mock(Artifact.class);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        artifacts.add(artifact2);

        Mockito.when(project.getArtifacts()).thenReturn(artifacts);
        Mockito.when(artifact.getFile()).thenReturn(file);
        Mockito.when(file.isFile()).thenReturn(true);
        Mockito.when(file.getName()).thenReturn("iamjar.jar");
        Mockito.when(artifact2.getFile()).thenReturn(file2);
        Mockito.when(file2.isDirectory()).thenReturn(true);

        final List<File> files = Util.getClassPath(project);
        Assert.assertEquals(2, files.size());
        Assert.assertTrue(files.contains(file) && files.contains(file2));
    }

    @Test
    public void checkClasspathTest() throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        final Plugin plugin = Mockito.mock(Plugin.class);
        final RepositorySystem repoSystem = Mockito.mock(RepositorySystem.class);
        final ArtifactRepository localRepo = Mockito.mock(ArtifactRepository.class);
        final ArtifactResolutionResult artifactResolResult = Mockito.mock(ArtifactResolutionResult.class);
        final Artifact artifact = Mockito.mock(Artifact.class);
        final Dependency dep = Mockito.mock(Dependency.class);

        final List<ArtifactRepository> remoteRepos = new ArrayList<>();
        remoteRepos.add(localRepo);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);

        final List<Dependency> listDepcy = new ArrayList<>();
        listDepcy.add(dep);

        Mockito.when(project.getPlugin(Mockito.anyString())).thenReturn(plugin);
        Mockito.when(plugin.getDependencies()).thenReturn(listDepcy);
        Mockito.when(artifact.getArtifactId()).thenReturn("artifactId");
        Mockito.when(artifact.getGroupId()).thenReturn("groupId");
        Mockito.when(artifact.getVersion()).thenReturn("SNAPSHOT");
        Mockito.when(repoSystem.createDependencyArtifact(dep)).thenReturn(artifact);
        Mockito.when(repoSystem.resolve(Mockito.any(ArtifactResolutionRequest.class))).thenReturn(artifactResolResult);
        Mockito.when(artifactResolResult.getArtifacts()).thenReturn(artifacts);
        Mockito.when(project.getDependencyArtifacts()).thenReturn(artifacts);

        Util.checkClasspath(project, repoSystem, localRepo, remoteRepos);
        Assert.assertEquals(1, artifacts.size());
        Assert.assertEquals(1, remoteRepos.size());
        Assert.assertEquals(1, listDepcy.size());
    }

    @Test
    public void findYangFilesInDependenciesAsStream() throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        prepareProject(project);

        final YangsInZipsResult yangzip = Util.findYangFilesInDependenciesAsStream(project);
        Assert.assertNotNull(yangzip);
        Assert.assertEquals(2, yangzip.getYangStreams().size());
        yangzip.close();
    }

    @Test
    public void findYangFilesInDependencies() throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        prepareProject(project);

        final Collection<File> files = Util.findYangFilesInDependencies(project);
        Assert.assertNotNull(files);
        Assert.assertEquals(2, files.size());
    }

    @Test
    public void contextHolderTest() throws Exception {
        final File testYang1 = new File(getClass().getResource("/test.yang").toURI());
        final File testYang2 = new File(getClass().getResource("/test2.yang").toURI());
        final SchemaContext context = YangParserTestUtils.parseYangSources(testYang1, testYang2);
        final Set<Module> yangModules = new HashSet<>();
        final Util.ContextHolder cxH = new ContextHolder(context, yangModules, yangModules);
        Assert.assertEquals(context, cxH.getContext());
        Assert.assertEquals(yangModules, cxH.getYangModules());
    }

    private URL makeMetaInf() throws Exception {
        final String path = getClass().getResource("/").getPath();
        final String metaInfPath = path + "tests/META-INF/yang";
        final Path createDirectories = Files.createDirectories(Paths.get(metaInfPath));
        Assert.assertNotNull(createDirectories);
        Assert.assertEquals(metaInfPath, createDirectories.toString());
        Runtime.getRuntime().exec("cp " + path + "/test.yang " + metaInfPath + "/");
        Runtime.getRuntime().exec("cp " + path + "/test2.yang " + metaInfPath + "/");
        return getClass().getResource("/tests");
    }

    private void prepareProject(final MavenProject project) throws Exception {
        URL url = getClass().getResource("/tests");
        if (url == null) {
            url = makeMetaInf();
        }
        Assert.assertNotNull(url);
        final File testFile = new File(getClass().getResource("/tests").toURI());
        File testFile2 = new File(getClass().getResource("/").getPath(), "test.jar");
        testFile2.createNewFile();
        testFile2 = new File(getClass().getResource("/test.jar").getFile());

        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        final JarOutputStream target = new JarOutputStream(new FileOutputStream(testFile2), manifest);
        addSourceFileToTargetJar(new File(getClass().getResource("/tests/META-INF").getPath()), target);
        target.close();

        final Artifact artifact = Mockito.mock(Artifact.class);
        final Artifact artifact2 = Mockito.mock(Artifact.class);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        artifacts.add(artifact2);

        Mockito.when(project.getArtifacts()).thenReturn(artifacts);
        Mockito.when(artifact.getFile()).thenReturn(testFile);
        Mockito.when(artifact2.getFile()).thenReturn(testFile2);
    }

    private void addSourceFileToTargetJar(final File source, final JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    final JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (final File nestedFile : source.listFiles()) {
                    addSourceFileToTargetJar(nestedFile, target);
                }
                return;
            }

            final JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            final byte[] buffer = new byte[1024];
            while (true) {
                final int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
