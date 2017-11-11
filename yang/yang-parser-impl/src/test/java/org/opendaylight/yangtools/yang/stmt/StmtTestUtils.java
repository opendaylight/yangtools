/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class StmtTestUtils {

    public static final FileFilter YANG_FILE_FILTER =
        file -> file.getName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();

    public static final FileFilter YIN_FILE_FILTER =
        file -> file.getName().endsWith(YangConstants.RFC6020_YIN_FILE_EXTENSION) && file.isFile();

    private static final Logger LOG = LoggerFactory.getLogger(StmtTestUtils.class);

    private StmtTestUtils() {

    }

    public static void log(final Throwable exception, final String indent) {
        LOG.debug(indent + exception.getMessage());

        final Throwable[] suppressed = exception.getSuppressed();
        for (final Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }
    }

    public static List<Module> findModules(final Set<Module> modules, final String moduleName) {
        final List<Module> result = new ArrayList<>();
        for (final Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result.add(module);
            }
        }
        return result;
    }

    public static StatementStreamSource sourceForResource(final String resourceName) {
        try {
            return YangStatementStreamSource.create(YangTextSchemaSource.forResource(resourceName));
        } catch (IOException | YangSyntaxErrorException e) {
            throw new IllegalArgumentException("Failed to create source", e);
        }
    }

    public static void printReferences(final Module module, final boolean isSubmodule, final String indent) {
        LOG.debug(indent + (isSubmodule ? "Submodule " : "Module ") + module.getName());
        final Set<Module> submodules = module.getSubmodules();
        for (final Module submodule : submodules) {
            printReferences(submodule, true, indent + "      ");
            printChilds(submodule.getChildNodes(), indent + "            ");
        }
    }

    public static void printChilds(final Collection<DataSchemaNode> childNodes, final String indent) {

        for (final DataSchemaNode child : childNodes) {
            LOG.debug(indent + "Child " + child.getQName().getLocalName());
            if (child instanceof DataNodeContainer) {
                printChilds(((DataNodeContainer) child).getChildNodes(), indent + "      ");
            }
        }
    }

    public static SchemaContext parseYangSource(final String yangSourcePath) throws ReactorException,
            URISyntaxException, IOException, YangSyntaxErrorException {
        return parseYangSource(yangSourcePath, StatementParserMode.DEFAULT_MODE, null);
    }

    public static SchemaContext parseYangSource(final String yangSourcePath, final Set<QName> supportedFeatures)
            throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        return parseYangSource(yangSourcePath, StatementParserMode.DEFAULT_MODE, supportedFeatures);
    }

    public static SchemaContext parseYangSource(final String yangSourcePath,
            final StatementParserMode statementParserMode, final Set<QName> supportedFeatures)
                    throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        final URL source = StmtTestUtils.class.getResource(yangSourcePath);
        final File sourceFile = new File(source.toURI());
        return parseYangSources(statementParserMode, supportedFeatures, sourceFile);
    }

    public static SchemaContext parseYangSources(final StatementStreamSource... sources) throws ReactorException {
        return parseYangSources(StatementParserMode.DEFAULT_MODE, null, sources);
    }

    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode,
            final Set<QName> supportedFeatures, final StatementStreamSource... sources) throws ReactorException {
        return parseYangSources(statementParserMode, supportedFeatures, Arrays.asList(sources));
    }

    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode,
            final Set<QName> supportedFeatures, final Collection<? extends StatementStreamSource> sources)
            throws ReactorException {
        final BuildAction reactor = DefaultReactors.defaultReactor().newBuild(statementParserMode)
                .addSources(sources);
        if (supportedFeatures != null) {
            reactor.setSupportedFeatures(supportedFeatures);
        }

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(final File... files) throws ReactorException, IOException,
            YangSyntaxErrorException {
        return parseYangSources(StatementParserMode.DEFAULT_MODE, null, files);
    }

    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode,
            final Set<QName> supportedFeatures, final File... files) throws  ReactorException, IOException,
            YangSyntaxErrorException {

        final Collection<YangStatementStreamSource> sources = new ArrayList<>(files.length);
        for (File file : files) {
            sources.add(YangStatementStreamSource.create(YangTextSchemaSource.forFile(file)));
        }

        return parseYangSources(statementParserMode, supportedFeatures, sources);
    }

    public static SchemaContext parseYangSources(final Collection<File> files) throws ReactorException, IOException,
            YangSyntaxErrorException {
        return parseYangSources(files, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSources(final Collection<File> files,
            final StatementParserMode statementParserMode) throws ReactorException, IOException,
            YangSyntaxErrorException {
        return parseYangSources(statementParserMode, null, files.toArray(new File[files.size()]));
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath) throws
            ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        return parseYangSources(yangSourcesDirectoryPath, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath,
            final StatementParserMode statementParserMode) throws ReactorException, URISyntaxException, IOException,
            YangSyntaxErrorException {
        return parseYangSources(yangSourcesDirectoryPath, null, statementParserMode);
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath,
            final Set<QName> supportedFeatures, final StatementParserMode statementParserMode) throws ReactorException,
            URISyntaxException, IOException, YangSyntaxErrorException {

        final URL resourceDir = StmtTestUtils.class.getResource(yangSourcesDirectoryPath);
        final File testSourcesDir = new File(resourceDir.toURI());

        return parseYangSources(statementParserMode, supportedFeatures, testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    public static SchemaContext parseYangSources(final String yangFilesDirectoryPath,
            final String yangLibsDirectoryPath)
            throws URISyntaxException, ReactorException, IOException, YangSyntaxErrorException {
        return parseYangSources(yangFilesDirectoryPath, yangLibsDirectoryPath, null);
    }

    public static SchemaContext parseYangSources(final String yangFilesDirectoryPath,
            final String yangLibsDirectoryPath, final Set<QName> supportedFeatures) throws URISyntaxException,
            ReactorException, IOException, YangSyntaxErrorException {
        final File yangsDir = new File(StmtTestUtils.class.getResource(yangFilesDirectoryPath).toURI());
        final File libsDir = new File(StmtTestUtils.class.getResource(yangLibsDirectoryPath).toURI());

        return parseYangSources(yangsDir.listFiles(YANG_FILE_FILTER), libsDir.listFiles(YANG_FILE_FILTER),
                supportedFeatures);
    }

    private static SchemaContext parseYangSources(final File[] yangFiles, final File[] libFiles,
            final Set<QName> supportedFeatures) throws ReactorException, IOException, YangSyntaxErrorException {
        final StatementStreamSource[] yangSources = new StatementStreamSource[yangFiles.length];
        for (int i = 0; i < yangFiles.length; i++) {
            yangSources[i] = YangStatementStreamSource.create(YangTextSchemaSource.forFile(yangFiles[i]));
        }

        final StatementStreamSource[] libSources = new StatementStreamSource[libFiles.length];
        for (int i = 0; i < libFiles.length; i++) {
            libSources[i] = YangStatementStreamSource.create(YangTextSchemaSource.forFile(libFiles[i]));
        }

        return parseYangSources(yangSources, libSources, supportedFeatures);
    }

    private static SchemaContext parseYangSources(final StatementStreamSource[] yangSources,
            final StatementStreamSource[] libSources, final Set<QName> supportedFeatures) throws ReactorException {

        final BuildAction reactor = DefaultReactors.defaultReactor().newBuild()
                .addSources(yangSources).addLibSources(libSources);
        if (supportedFeatures != null) {
            reactor.setSupportedFeatures(supportedFeatures);
        }

        return reactor.buildEffective();
    }

    public static SchemaContext parseYinSources(final String yinSourcesDirectoryPath,
            final StatementParserMode statementParserMode) throws URISyntaxException, SAXException, IOException,
            ReactorException {
        final URL resourceDir = StmtTestUtils.class.getResource(yinSourcesDirectoryPath);
        final File[] files = new File(resourceDir.toURI()).listFiles(YIN_FILE_FILTER);
        final StatementStreamSource[] sources = new StatementStreamSource[files.length];
        for (int i = 0; i < files.length; i++) {
            final SourceIdentifier identifier = YinTextSchemaSource.identifierFromFilename(files[i].getName());

            sources[i] = YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                YinTextSchemaSource.delegateForByteSource(identifier, Files.asByteSource(files[i]))));
        }

        return parseYinSources(statementParserMode, sources);
    }

    public static SchemaContext parseYinSources(final StatementParserMode statementParserMode,
            final StatementStreamSource... sources) throws ReactorException {
        return DefaultReactors.defaultReactor().newBuild(statementParserMode).addSources(sources).buildEffective();
    }

    public static Module findImportedModule(final SchemaContext context, final Module rootModule,
            final String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        final Set<ModuleImport> rootImports = rootModule.getImports();
        for (final ModuleImport moduleImport : rootImports) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        return context.findModule(requestedModuleImport.getModuleName(), requestedModuleImport.getRevision())
                .orElse(null);
    }
}
