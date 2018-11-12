/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
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
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator.ImportResolutionMode;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * A generator task performed using {@link FileGenerator}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class FileGeneratorTask extends AbstractGeneratorTask {
    private static final Logger LOG = LoggerFactory.getLogger(FileGeneratorTask.class);
    private static final CharMatcher SEP_MATCHER = CharMatcher.is(GeneratedFilePath.SEPARATOR);
    private static final Map<GeneratedFileType, String> TYPE_PATHS = ImmutableMap.of(
        GeneratedFileType.RESOURCE, "generated-resources", GeneratedFileType.TEST_RESOURCE, "generated-test-resources",
        GeneratedFileType.SOURCE, "generated-sources", GeneratedFileType.TEST_SOURCE, "generated-test-sources");

    private final FileGeneratorArg arg;
    private final FileGenerator gen;

    private @Nullable File buildDir;
    private @Nullable ContextHolder context;

    private FileGeneratorTask(final StatementParserMode parserMode, final FileGeneratorArg arg,
            final FileGenerator gen) {
        super(parserMode);
        this.arg = arg;
        this.gen = gen;
    }

    static FileGeneratorTask of(final FileGeneratorFactory factory, final FileGeneratorArg arg)
            throws FileGeneratorException {
        final FileGenerator gen = factory.newFileGenerator(arg.getConfiguration());

        final StatementParserMode parserMode;
        final ImportResolutionMode importMode = gen.importResolutionMode();
        switch (importMode) {
            case REVISION_EXACT_OR_LATEST:
                parserMode = StatementParserMode.DEFAULT_MODE;
                break;
            case SEMVER_LATEST:
                parserMode = StatementParserMode.SEMVER_MODE;
                break;
            default:
                throw new LinkageError("Unhandled import mode " + importMode);
        }

        return new FileGeneratorTask(parserMode, arg, gen);
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
    Collection<File> execute(final BuildContext buildContext) throws FileGeneratorException, IOException {
        final ContextHolder local = verifyNotNull(context);

        final Stopwatch watch = Stopwatch.createStarted();
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generatedFiles = gen.generateFiles(
            local.getContext(), local.getYangModules(), local);
        LOG.info("Generator {} defined {} files in {}", arg.getIdentifier(), generatedFiles.size(), watch);

        final List<File> ret = new ArrayList<>(generatedFiles.size());
        for (Entry<GeneratedFileType, Map<GeneratedFilePath, GeneratedFile>> row : generatedFiles.rowMap().entrySet()) {
            final File typeFile = typePath(row.getKey());

            for (Entry<GeneratedFilePath, GeneratedFile> entry : row.getValue().entrySet()) {
                final File target = new File(typeFile, SEP_MATCHER.replaceFrom(entry.getKey().getPath(),
                    File.separatorChar));
                ret.add(target);
                final GeneratedFile file = entry.getValue();
                if (GeneratedFileLifecycle.PERSISTENT == file.getLifecycle() && target.exists()) {
                    LOG.debug("Skipping persistent file {}", file);
                    continue;
                }

                file.writeBody(buildContext.newFileOutputStream(target));
            }
        }
        LOG.info("Wrote {} files from generator {} in {}", generatedFiles.size(), arg.getIdentifier(), watch);

        return ret;
    }

    private File typePath(final GeneratedFileType fileType) {
        String path = TYPE_PATHS.get(fileType);
        if (path == null) {
            // FIXME: lookup in configuration
            throw new UnsupportedOperationException();
        }

        return new File(buildDir, path + File.separatorChar + arg.getIdentifier());
    }
}
