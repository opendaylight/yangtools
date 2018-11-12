/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.yang.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFileSpec;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * A generator task performed using {@link FileGenerator}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class FileGeneratorTask extends AbstractGeneratorTask {
    private static final CharMatcher SLASH_MATCHER = CharMatcher.is('/');
    private static final Map<GeneratedFileType, String> TYPE_PATHS = ImmutableMap.of(
        GeneratedFileType.RESOURCE, "generated-resources", GeneratedFileType.TEST_RESOURCE, "generated-test-resources",
        GeneratedFileType.SOURCE, "generated-sources", GeneratedFileType.TEST_SOURCE, "generated-test-sources");

    private final FileGeneratorArg arg;
    private final FileGenerator gen;

    private @Nullable File buildDir;
    private @Nullable ContextHolder context;

    FileGeneratorTask(final FileGeneratorArg arg, final FileGeneratorFactory factory) {
        this.arg = requireNonNull(arg);
        this.gen = factory.newFileGenerator(arg.getConfiguration());
    }

    @Override
    void initialize(final MavenProject project, final ContextHolder context) {
        this.context = requireNonNull(context);
        buildDir = new File(project.getModel().getBuild().getDirectory());

        if (gen instanceof MavenProjectAware) {
            ((MavenProjectAware)gen).setMavenProject(project);
        }
    }

    @Override
    Collection<File> execute(final BuildContext buildContext) throws IOException {
        final Map<GeneratedFileSpec, GeneratedFile> generatedFiles = gen.generateFiles(context.getContext(),
            context.getYangModules(), context::moduleToResourcePath);
        final List<File> ret = new ArrayList<>(generatedFiles.size());
        for (Entry<GeneratedFileSpec, GeneratedFile> entry : generatedFiles.entrySet()) {
            final File file = new File(buildDir, typePath(entry.getKey()));
            entry.getValue().writeBody(buildContext.newFileOutputStream(file));
            ret.add(file);
        }

        return ret;
    }

    private String typePath(final GeneratedFileSpec key) {
        // TODO Auto-generated method stub
        final File file = new File(baseFile(cell.getRowKey(), cell.getValue().getLifecycle()),
            SLASH_MATCHER.replaceFrom(cell.getColumnKey(), File.separator));
        cell.getValue().writeBody();
        return null;
    }
}
