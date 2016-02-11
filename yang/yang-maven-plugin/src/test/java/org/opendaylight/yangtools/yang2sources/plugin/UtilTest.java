/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang2sources.plugin.Util.ContextHolder;
import org.opendaylight.yangtools.yang2sources.plugin.Util.YangsInZipsResult;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {

    @Test
    public void testCache() throws Exception {
        final String yang = new File(getClass().getResource("/yang/mock.yang").toURI()).getParent();
        final Collection<File> files = Util.listFiles(new File(yang));
        final Collection<File> files2 = Util.listFiles(new File(yang));
        Assert.assertTrue(files == files2);
    }

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
        final File testFile = new File(getClass().getResource("/tests").toURI());
        final File testFile2 = new File(getClass().getResource("/test.jar").toURI());
        final Artifact artifact = Mockito.mock(Artifact.class);
        final Artifact artifact2 = Mockito.mock(Artifact.class);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        artifacts.add(artifact2);

        Mockito.when(project.getArtifacts()).thenReturn(artifacts);
        Mockito.when(artifact.getFile()).thenReturn(testFile);
        Mockito.when(artifact2.getFile()).thenReturn(testFile2);

        final YangsInZipsResult yangzip = Util.findYangFilesInDependenciesAsStream(project);
        Assert.assertNotNull(yangzip);
        Assert.assertEquals(4, yangzip.getYangStreams().size());

    }

    @Test
    public void contextHolderTest() throws Exception {
        final File testYang1 = new File(getClass().getResource("/test.yang").toURI());
        final File testYang2 = new File(getClass().getResource("/test2.yang").toURI());
        final SchemaContext context = parseYangSources(testYang1, testYang2);
        final Set<Module> yangModules = new HashSet<>();
        final Util.ContextHolder cxH = new ContextHolder(context, yangModules);
        Assert.assertEquals(context, cxH.getContext());
        Assert.assertEquals(yangModules, cxH.getYangModules());
    }

    private SchemaContext parseYangSources(final File... files)
            throws SourceException, ReactorException, FileNotFoundException {

        final StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new FileInputStream(files[i]));
        }

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(sources);

        return reactor.buildEffective();
    }
}
