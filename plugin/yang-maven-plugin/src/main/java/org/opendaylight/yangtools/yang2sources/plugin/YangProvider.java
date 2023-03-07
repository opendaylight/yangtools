/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class YangProvider {
    private static final class Default extends YangProvider {
        private static final Logger LOG = LoggerFactory.getLogger(Default.class);

        @Override
        Collection<FileState> addYangsToMetaInf(final MavenProject project,
                final Collection<YangTextSchemaSource> modelsInProject) throws IOException {

            final File generatedYangDir =
                // FIXME: why are we generating these in "generated-sources"? At the end of the day YANG files are more
                //        resources (except we do not them to be subject to filtering)
                new File(new File(project.getBuild().getDirectory(), "generated-sources"), "yang");
            LOG.debug("Generated dir {}", generatedYangDir);

            // copy project's src/main/yang/*.yang to ${project.builddir}/generated-sources/yang/META-INF/yang/
            // This honors setups like a Eclipse-profile derived one
            final File withMetaInf = new File(generatedYangDir, YangToSourcesProcessor.META_INF_YANG_STRING);
            final var stateListBuilder = ImmutableList.<FileState>builderWithExpectedSize(modelsInProject.size());

            for (YangTextSchemaSource source : modelsInProject) {
                final File file = new File(withMetaInf, source.getIdentifier().toYangFilename());
                Files.createParentDirs(file);

                stateListBuilder.add(FileState.ofWrittenFile(file, source::copyTo));
                LOG.debug("Created file {} for {}", file, source.getIdentifier());
            }

            setResource(generatedYangDir, project);
            LOG.debug("{} YANG files marked as resources: {}", YangToSourcesProcessor.LOG_PREFIX, generatedYangDir);

            return stateListBuilder.build();
        }
    }

    private static final YangProvider DEFAULT = new Default();

    static YangProvider getInstance() {
        return DEFAULT;
    }

    abstract Collection<FileState> addYangsToMetaInf(MavenProject project,
            Collection<YangTextSchemaSource> modelsInProject) throws IOException;

    static void setResource(final File targetYangDir, final MavenProject project) {
        Resource res = new Resource();
        res.setDirectory(targetYangDir.getPath());
        project.addResource(res);
    }
}
