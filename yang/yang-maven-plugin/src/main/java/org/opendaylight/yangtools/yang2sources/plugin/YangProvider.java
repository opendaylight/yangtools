/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class YangProvider {
    private static final Logger LOG = LoggerFactory.getLogger(YangProvider.class);

    void addYangsToMetaInf(final MavenProject project, final File yangFilesRootDir,
            final Collection<File> excludedFiles) throws MojoFailureException {

        // copy project's src/main/yang/*.yang to target/generated-sources/yang/META-INF/yang/*.yang
        File generatedYangDir = new GeneratedDirectories(project).getYangDir();
        addYangsToMetaInf(project, yangFilesRootDir, excludedFiles, generatedYangDir);
    }

    private static void addYangsToMetaInf(final MavenProject project, final File yangFilesRootDir,
            final Collection<File> excludedFiles, final File generatedYangDir) throws MojoFailureException {

        File withMetaInf = new File(generatedYangDir, YangToSourcesProcessor.META_INF_YANG_STRING);
        withMetaInf.mkdirs();

        try {
            Collection<File> files = Util.listFiles(yangFilesRootDir, excludedFiles);
            for (File file : files) {
                org.apache.commons.io.FileUtils.copyFile(file, new File(withMetaInf, file.getName()));
            }
        } catch (IOException e) {
            LOG.warn("Failed to generate files into root {}", yangFilesRootDir, e);
            throw new MojoFailureException("Unable to list yang files into resource folder", e);
        }

        setResource(generatedYangDir, project);

        LOG.debug("{} Yang files from: {} marked as resources: {}", YangToSourcesProcessor.LOG_PREFIX, yangFilesRootDir,
                YangToSourcesProcessor.META_INF_YANG_STRING_JAR);
    }

    static void setResource(final File targetYangDir, final MavenProject project) {
        Resource res = new Resource();
        res.setDirectory(targetYangDir.getPath());
        project.addResource(res);
    }
}
