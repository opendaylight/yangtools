/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.FileYinTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class StmtTestUtils {

    public static final FileFilter YANG_FILE_FILTER =
        file -> file.getName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();

    public static final FileFilter YIN_FILE_FILTER =
        file -> file.getName().endsWith(YangConstants.RFC6020_YIN_FILE_EXTENSION) && file.isFile();

    private static final Logger LOG = LoggerFactory.getLogger(StmtTestUtils.class);

    private StmtTestUtils() {

    }

    public static void log(final Throwable exception, final String indent) {
        LOG.debug("{}{}", indent, exception.getMessage());

        final Throwable[] suppressed = exception.getSuppressed();
        for (final Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }
    }

    public static YangStatementStreamSource sourceForResource(final String resourceName) {
        try {
            return YangStatementStreamSource.create(YangTextSource.forPath(Path.of(
                StmtTestUtils.class.getResource(resourceName).toURI())));
        } catch (IOException | YangSyntaxErrorException | URISyntaxException e) {
            throw new IllegalArgumentException("Failed to create source", e);
        }
    }

    public static EffectiveModelContext parseYangSource(final String yangSourcePath, final Set<QName> supportedFeatures)
            throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        return parseYangSource(yangSourcePath, YangParserConfiguration.DEFAULT, supportedFeatures);
    }

    public static EffectiveModelContext parseYangSource(final String yangSourcePath,
            final YangParserConfiguration config, final Set<QName> supportedFeatures)
                    throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        return parseYangSources(config, supportedFeatures,
            new File(StmtTestUtils.class.getResource(yangSourcePath).toURI()));
    }

    public static EffectiveModelContext parseYangSources(final StatementStreamSource... sources)
            throws ReactorException {
        return parseYangSources(YangParserConfiguration.DEFAULT, null, sources);
    }

    public static EffectiveModelContext parseYangSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final StatementStreamSource... sources) throws ReactorException {
        return parseYangSources(config, supportedFeatures, Arrays.asList(sources));
    }

    public static EffectiveModelContext parseYangSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final Collection<? extends StatementStreamSource> sources)
            throws ReactorException {
        final BuildAction build = getReactor(config).newBuild().addSources(sources);
        if (supportedFeatures != null) {
            build.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }
        return build.buildEffective();
    }

    public static EffectiveModelContext parseYangSources(final File... files) throws ReactorException, IOException,
            YangSyntaxErrorException {
        return parseYangSources(YangParserConfiguration.DEFAULT, null, files);
    }

    public static EffectiveModelContext parseYangSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final File... files) throws  ReactorException, IOException,
            YangSyntaxErrorException {

        final Collection<YangStatementStreamSource> sources = new ArrayList<>(files.length);
        for (File file : files) {
            sources.add(YangStatementStreamSource.create(YangTextSource.forPath(file.toPath())));
        }

        return parseYangSources(config, supportedFeatures, sources);
    }

    public static EffectiveModelContext parseYangSources(final String yangSourcesDirectoryPath,
            final YangParserConfiguration config) throws ReactorException, URISyntaxException, IOException,
            YangSyntaxErrorException {
        return parseYangSources(yangSourcesDirectoryPath, null, config);
    }

    public static EffectiveModelContext parseYangSources(final String yangSourcesDirectoryPath,
            final Set<QName> supportedFeatures, final YangParserConfiguration config) throws ReactorException,
            URISyntaxException, IOException, YangSyntaxErrorException {

        final URL resourceDir = StmtTestUtils.class.getResource(yangSourcesDirectoryPath);
        final File testSourcesDir = new File(resourceDir.toURI());

        return parseYangSources(config, supportedFeatures, testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    public static EffectiveModelContext parseYinSources(final String yinSourcesDirectoryPath)
            throws URISyntaxException, SAXException, IOException, ReactorException {
        return parseYinSources(yinSourcesDirectoryPath, YangParserConfiguration.DEFAULT);
    }

    public static EffectiveModelContext parseYinSources(final String yinSourcesDirectoryPath,
            final YangParserConfiguration config)
                throws URISyntaxException, SAXException, IOException, ReactorException {
        final var resourceDir = StmtTestUtils.class.getResource(yinSourcesDirectoryPath);
        final var files = new File(resourceDir.toURI()).listFiles(YIN_FILE_FILTER);
        final var sources = new StatementStreamSource[files.length];
        for (int i = 0; i < files.length; i++) {
            sources[i] = YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                new FileYinTextSource(files[i].toPath())));
        }

        return parseYinSources(config, sources);
    }

    public static EffectiveModelContext parseYinSources(final YangParserConfiguration config,
            final StatementStreamSource... sources) throws ReactorException {
        return getReactor(config)
            .newBuild()
            .addSources(sources)
            .buildEffective();
    }

    public static Module findImportedModule(final SchemaContext context, final Module rootModule,
            final String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        for (final ModuleImport moduleImport : rootModule.getImports()) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        return context.findModule(requestedModuleImport.getModuleName().getLocalName(),
                    requestedModuleImport.getRevision())
                .orElse(null);
    }

    private static CrossSourceStatementReactor getReactor(final YangParserConfiguration config) {
        return YangParserConfiguration.DEFAULT.equals(config) ? RFC7950Reactors.defaultReactor()
            : RFC7950Reactors.defaultReactorBuilder(config).build();
    }
}
