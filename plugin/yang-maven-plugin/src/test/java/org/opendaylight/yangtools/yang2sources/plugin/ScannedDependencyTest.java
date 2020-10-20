/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class ScannedDependencyTest {

    @Test
    public void findYangFilesInDependenciesAsStream() throws Exception {
        final MavenProject project = mock(MavenProject.class);
        prepareProject(project);

        final Collection<ScannedDependency> yangzip = ScannedDependency.scanDependencies(project);
        assertNotNull(yangzip);
        assertEquals(2, yangzip.size());
    }

    @Test
    public void findYangFilesInDependencies() throws Exception {
        final MavenProject project = mock(MavenProject.class);
        prepareProject(project);

        final Collection<ScannedDependency> files = ScannedDependency.scanDependencies(project);
        assertNotNull(files);
        assertEquals(2, files.size());
    }

    private static void prepareProject(final MavenProject project) throws Exception {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        final File testFile2 = new File(ScannedDependencyTest.class.getResource("/").getPath(), "test.jar");
        final JarOutputStream target = new JarOutputStream(new FileOutputStream(testFile2), manifest);
        addSourceFileToTargetJar(new File(ScannedDependencyTest.class.getResource("/tests/META-INF").getPath()),
            target);
        target.close();

        final Artifact artifact = mock(Artifact.class);
        when(artifact.getFile()).thenReturn(new File(ScannedDependencyTest.class.getResource("/tests").toURI()));

        final Artifact artifact2 = mock(Artifact.class);
        when(artifact2.getFile()).thenReturn(testFile2);
        when(project.getArtifacts()).thenReturn(ImmutableSet.of(artifact, artifact2));
    }

    private static void addSourceFileToTargetJar(final File source, final JarOutputStream target) throws IOException {
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
