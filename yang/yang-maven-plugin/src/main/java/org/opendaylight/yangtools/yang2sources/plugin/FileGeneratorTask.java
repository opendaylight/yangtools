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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFileKind;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * A generator task performed using {@link FileGenerator}.
 *
 * @author Robert Varga
 */
final class FileGeneratorTask extends AbstractGeneratorTask {
    private static final CharMatcher SLASH_MATCHER = CharMatcher.is('/');

    private static final Table<GeneratedFileKind, GeneratedFileLifecycle, String> FILES =
            ImmutableTable.<GeneratedFileKind, GeneratedFileLifecycle, String>builder()
            .put(GeneratedFileKind.SOURCE, GeneratedFileLifecycle.PERSISTENT, "src/main")
            .put(GeneratedFileKind.SOURCE, GeneratedFileLifecycle.TRANSIENT, "target/generated-sources")
            .put(GeneratedFileKind.TEST_SOURCE, GeneratedFileLifecycle.PERSISTENT, "src/test")
            .put(GeneratedFileKind.TEST_SOURCE, GeneratedFileLifecycle.TRANSIENT, "target/generated-test-sources")
            .put(GeneratedFileKind.RESOURCE, GeneratedFileLifecycle.PERSISTENT, "src/main/resources")
            .put(GeneratedFileKind.RESOURCE, GeneratedFileLifecycle.TRANSIENT, "target/generated-resources")
            .put(GeneratedFileKind.TEST_RESOURCE, GeneratedFileLifecycle.PERSISTENT, "src/test/resources")
            .put(GeneratedFileKind.TEST_RESOURCE, GeneratedFileLifecycle.TRANSIENT, "target/generated-test-resources")
            .build();

    private final FileGenerator gen;

    private ContextHolder context;

    FileGeneratorTask(final FileGenerator gen) {
        this.gen = requireNonNull(gen);
    }

    @Override
    void initialize(final MavenProject project, final ContextHolder context) {
        this.context = requireNonNull(context);

        if (gen instanceof MavenProjectAware) {
            ((MavenProjectAware)gen).setMavenProject(project);
        }
    }

    @Override
    Optional<ImportResolutionMode> suggestedImportResolutionMode() {
        return gen.suggestedImportResolutionMode();
    }

    @Override
    boolean isAcceptableImportResolutionMode(final ImportResolutionMode mode) {
        return gen.isAcceptableImportResolutionMode(mode);
    }

    @Override
    Collection<File> execute(final BuildContext buildContext) throws IOException {
        final Table<GeneratedFileKind, String, GeneratedFile> generatedFiles = gen.generateFiles(
            context.getContext(), context.getYangModules(), context::moduleToResourcePath);

        final Builder<File> ret = ImmutableList.builder();
        for (Cell<GeneratedFileKind, String, GeneratedFile> cell : generatedFiles.cellSet()) {
            final File file = new File(baseFile(cell.getRowKey(), cell.getValue().getLifecycle()),
                SLASH_MATCHER.replaceFrom(cell.getColumnKey(), File.separator));
            cell.getValue().writeBody(buildContext.newFileOutputStream(file));
            ret.add(file);
        }

        return ret.build();
    }

    private static File baseFile(final GeneratedFileKind kind, final GeneratedFileLifecycle lifecycle) {
        final String path;
        switch (kind) {
            case RESOURCE:
                switch (lifecycle) {
                    case PERSISTENT:
                        path = "src/main/resources";
                        break;
                    case TRANSIENT:
                        // FIXME: pick up build directory
                        path = "target/generated-resources";
                    default:
                        throw new IllegalStateException("Unhandled lifecycle " + lifecycle);
                }
                break;
            case SOURCE:
                switch (lifecycle) {
                    case PERSISTENT:
                        path = "src/main";
                        break;
                    case TRANSIENT:
                        // FIXME: pick up build directory
                        path = "target/generated-sources";
                    default:
                        throw new IllegalStateException("Unhandled lifecycle " + lifecycle);
                }
                break;
            case TEST_RESOURCE:
                switch (lifecycle) {
                    case PERSISTENT:
                        path = "src/test/resources";
                        break;
                    case TRANSIENT:
                        // FIXME: pick up build directory
                        path = "target/generated-test-sources";
                    default:
                        throw new IllegalStateException("Unhandled lifecycle " + lifecycle);
                }
                break;
            case TEST_SOURCE:
                switch (lifecycle) {
                    case PERSISTENT:
                        path = "src/test";
                        break;
                    case TRANSIENT:
                        // FIXME: pick up build directory
                        path = "target/generated-test-sources";
                    default:
                        throw new IllegalStateException("Unhandled lifecycle " + lifecycle);
                }
                break;
            default:
                throw new IllegalStateException("Unhandled file kind " + kind);
        }

        return new File(path);
    }
}
