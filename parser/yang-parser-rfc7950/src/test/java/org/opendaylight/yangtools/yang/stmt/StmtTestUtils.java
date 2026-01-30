/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StmtTestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(StmtTestUtils.class);

    public static final FileFilter YANG_FILE_FILTER =
        file -> file.getName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();

    public static final FileFilter YIN_FILE_FILTER =
        file -> file.getName().endsWith(YangConstants.RFC6020_YIN_FILE_EXTENSION) && file.isFile();

    private StmtTestUtils() {
        // Hidden on purpose
    }

    public static void log(final Throwable exception, final String indent) {
        LOG.debug("{}{}", indent, exception.getMessage());

        final Throwable[] suppressed = exception.getSuppressed();
        for (final Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }
    }

    @NonNullByDefault
    public static YangIRSource sourceForResource(final String resourceName) {
        return assertDoesNotThrow(() -> TestUtils.assertYangSource(resourceName));
    }

    public static EffectiveModelContext parseYangSource(final String yangSourcePath, final Set<QName> supportedFeatures)
            throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException, ExtractorException,
                   SourceSyntaxException {
        return parseYangSource(yangSourcePath, YangParserConfiguration.DEFAULT, supportedFeatures);
    }

    public static EffectiveModelContext parseYangSource(final String yangSourcePath,
            final YangParserConfiguration config, final Set<QName> supportedFeatures)
                throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException, ExtractorException,
                       SourceSyntaxException {
        return parseYangSources(config, supportedFeatures,
            Path.of(StmtTestUtils.class.getResource(yangSourcePath).toURI()).toFile());
    }

    public static EffectiveModelContext parseYangSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final Collection<? extends @NonNull YangIRSource> sources)
                throws ExtractorException, IOException, ReactorException, SourceSyntaxException {
        final var build = getReactor(config).newBuild();
        for (var source : sources) {
            build.addSource(source);
        }
        if (supportedFeatures != null) {
            build.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }
        return build.buildEffective();
    }

    public static EffectiveModelContext parseYangSources(final File... files) throws ReactorException, IOException,
            YangSyntaxErrorException, ExtractorException, SourceSyntaxException {
        return parseYangSources(YangParserConfiguration.DEFAULT, null, files);
    }

    public static EffectiveModelContext parseYangSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final File... files)
                throws ReactorException, IOException, YangSyntaxErrorException, ExtractorException,
                       SourceSyntaxException {
        final var sources = new ArrayList<YangIRSource>(files.length);
        for (var file : files) {
            sources.add(TestUtils.assertYangSource(file.toPath()));
        }
        return parseYangSources(config, supportedFeatures, sources);
    }

    public static EffectiveModelContext parseYangSources(final String yangSourcesDirectoryPath,
            final YangParserConfiguration config)
                throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException, ExtractorException,
                       SourceSyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, null, config);
    }

    public static EffectiveModelContext parseYangSources(final String yangSourcesDirectoryPath,
            final Set<QName> supportedFeatures, final YangParserConfiguration config)
                throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException, ExtractorException,
                       SourceSyntaxException {
        final var testSourcesDir = Path.of(StmtTestUtils.class.getResource(yangSourcesDirectoryPath).toURI()).toFile();
        return parseYangSources(config, supportedFeatures, testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    public static EffectiveModelContext parseYinSources(final String yinSourcesDirectoryPath)
            throws ExtractorException, IOException, ReactorException, SourceSyntaxException, URISyntaxException {
        return parseYinSources(yinSourcesDirectoryPath, YangParserConfiguration.DEFAULT);
    }

    public static EffectiveModelContext parseYinSources(final String yinSourcesDirectoryPath,
            final YangParserConfiguration config)
                throws ExtractorException, IOException, ReactorException, SourceSyntaxException, URISyntaxException {
        final var files = Path.of(StmtTestUtils.class.getResource(yinSourcesDirectoryPath).toURI()).toFile()
            .listFiles(YIN_FILE_FILTER);

        final var build = getReactor(config).newBuild();
        for (var file : files) {
            build.addSource(TestUtils.assertYinSource(file.toPath()));
        }
        return build.buildEffective();
    }

    public static Module findImportedModule(final SchemaContext context, final Module rootModule,
            final String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        for (var moduleImport : rootModule.getImports()) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        return context.findModule(requestedModuleImport.getModuleName().getLocalName(),
            requestedModuleImport.getRevision()).orElse(null);
    }

    private static CrossSourceStatementReactor getReactor(final YangParserConfiguration config) {
        return YangParserConfiguration.DEFAULT.equals(config) ? RFC7950Reactors.defaultReactor()
            : RFC7950Reactors.defaultReactorBuilder(config).build();
    }
}
