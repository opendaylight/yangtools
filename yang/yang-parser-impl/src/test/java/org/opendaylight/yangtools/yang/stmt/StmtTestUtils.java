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
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.IfFeaturePredicates;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YinStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StmtTestUtils {

    final public static FileFilter YANG_FILE_FILTER = file -> {
        final String name = file.getName().toLowerCase();
        return name.endsWith(".yang") && file.isFile();
    };

    final public static FileFilter YIN_FILE_FILTER = file -> {
        final String name = file.getName().toLowerCase();
        return name.endsWith(".xml") && file.isFile();
    };

    private static final Logger LOG = LoggerFactory.getLogger(StmtTestUtils.class);

    private StmtTestUtils() {

    }

    public static void log(final Throwable e, final String indent) {
        LOG.debug(indent + e.getMessage());

        final Throwable[] suppressed = e.getSuppressed();
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

    public static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (final YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
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

    public static SchemaContext parseYangSources(final StatementStreamSource... sources) throws SourceException,
            ReactorException {
        return parseYangSources(StatementParserMode.DEFAULT_MODE, IfFeaturePredicates.ALL_FEATURES, sources);
    }

    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode, final Predicate<QName> ifFeaturePredicate, final StatementStreamSource... sources)
            throws SourceException, ReactorException {

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(statementParserMode, ifFeaturePredicate);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(final File... files) throws SourceException, ReactorException,
            FileNotFoundException {
        return parseYangSources(StatementParserMode.DEFAULT_MODE, IfFeaturePredicates.ALL_FEATURES, files);
    }

    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode, final Predicate<QName> ifFeaturePredicate, final File... files) throws SourceException,
            ReactorException, FileNotFoundException {

        final StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYangSources(statementParserMode, ifFeaturePredicate, sources);
    }

    public static SchemaContext parseYangSources(final Collection<File> files) throws SourceException, ReactorException,
            FileNotFoundException {
        return parseYangSources(files, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSources(final Collection<File> files, final StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException {
        return parseYangSources(statementParserMode, IfFeaturePredicates.ALL_FEATURES, files.toArray(new File[files.size()]));
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath) throws SourceException,
            ReactorException, FileNotFoundException, URISyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSource(final String yangSourcePath) throws SourceException, ReactorException,
            FileNotFoundException, URISyntaxException {
        return parseYangSource(yangSourcePath, StatementParserMode.DEFAULT_MODE, IfFeaturePredicates.ALL_FEATURES);
    }

    public static SchemaContext parseYangSource(final String yangSourcePath, final Predicate<QName> ifFeaturesPredicate)
            throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        return parseYangSource(yangSourcePath, StatementParserMode.DEFAULT_MODE, ifFeaturesPredicate);
    }

    public static SchemaContext parseYangSource(final String yangSourcePath,
            final StatementParserMode statementParserMode, final Predicate<QName> ifFeaturePredicate)
            throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {
        final URL source = StmtTestUtils.class.getResource(yangSourcePath);
        final File sourceFile = new File(source.toURI());
        return parseYangSources(statementParserMode, ifFeaturePredicate, sourceFile);
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath, final StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {

        final URL resourceDir = StmtTestUtils.class.getResource(yangSourcesDirectoryPath);
        final File testSourcesDir = new File(resourceDir.toURI());

        return parseYangSources(statementParserMode, IfFeaturePredicates.ALL_FEATURES, testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    public static SchemaContext parseYinSources(final String yinSourcesDirectoryPath, final StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {

        final URL resourceDir = StmtTestUtils.class.getResource(yinSourcesDirectoryPath);
        final File testSourcesDir = new File(resourceDir.toURI());

        return parseYinSources(statementParserMode, testSourcesDir.listFiles(YIN_FILE_FILTER));
    }

    public static SchemaContext parseYinSources(final StatementParserMode statementParserMode, final File... files) throws SourceException,
            ReactorException, FileNotFoundException {

        final StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YinStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYinSources(statementParserMode, sources);
    }

    public static SchemaContext parseYinSources(final StatementParserMode statementParserMode, final StatementStreamSource... sources)
            throws SourceException, ReactorException {

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(statementParserMode);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static Module findImportedModule(final SchemaContext context, final Module rootModule, final String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        final Set<ModuleImport> rootImports = rootModule.getImports();
        for (final ModuleImport moduleImport : rootImports) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        final Module importedModule = context.findModuleByName(requestedModuleImport.getModuleName(),
                requestedModuleImport.getRevision());
        return importedModule;
    }
}
