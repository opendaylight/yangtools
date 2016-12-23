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
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
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

    final public static FileFilter YANG_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            String name = file.getName().toLowerCase();
            return name.endsWith(".yang") && file.isFile();
        }
    };

    final public static FileFilter YIN_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            String name = file.getName().toLowerCase();
            return name.endsWith(".xml") && file.isFile();
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(StmtTestUtils.class);

    private StmtTestUtils() {

    }

    public static void log(Throwable e, String indent) {
        LOG.debug(indent + e.getMessage());

        Throwable[] suppressed = e.getSuppressed();
        for (Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }
    }

    public static List<Module> findModules(final Set<Module> modules, final String moduleName) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result.add(module);
            }
        }
        return result;
    }

    public static void addSources(CrossSourceStatementReactor.BuildAction reactor, YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

    public static void printReferences(Module module, boolean isSubmodule, String indent) {
        LOG.debug(indent + (isSubmodule ? "Submodule " : "Module ") + module.getName());
        Set<Module> submodules = module.getSubmodules();
        for (Module submodule : submodules) {
            printReferences(submodule, true, indent + "      ");
            printChilds(submodule.getChildNodes(), indent + "            ");
        }
    }

    public static void printChilds(Collection<DataSchemaNode> childNodes, String indent) {

        for (DataSchemaNode child : childNodes) {
            LOG.debug(indent + "Child " + child.getQName().getLocalName());
            if (child instanceof DataNodeContainer) {
                printChilds(((DataNodeContainer) child).getChildNodes(), indent + "      ");
            }
        }
    }

    public static SchemaContext parseYangSources(StatementStreamSource... sources) throws SourceException,
            ReactorException {
        return parseYangSources(StatementParserMode.DEFAULT_MODE, sources);
    }

    public static SchemaContext parseYangSources(StatementParserMode statementParserMode, StatementStreamSource... sources)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(statementParserMode);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(File... files) throws SourceException, ReactorException,
            FileNotFoundException {
        return parseYangSources(StatementParserMode.DEFAULT_MODE, files);
    }

    public static SchemaContext parseYangSources(StatementParserMode statementParserMode, File... files) throws SourceException,
            ReactorException, FileNotFoundException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYangSources(statementParserMode, sources);
    }

    public static SchemaContext parseYangSources(Collection<File> files) throws SourceException, ReactorException,
            FileNotFoundException {
        return parseYangSources(files, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSources(Collection<File> files, StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException {
        return parseYangSources(statementParserMode, files.toArray(new File[files.size()]));
    }

    public static SchemaContext parseYangSources(String yangSourcesDirectoryPath) throws SourceException,
            ReactorException, FileNotFoundException, URISyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSources(String yangSourcesDirectoryPath, StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {

        URL resourceDir = StmtTestUtils.class.getResource(yangSourcesDirectoryPath);
        File testSourcesDir = new File(resourceDir.toURI());

        return parseYangSources(statementParserMode, testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    public static SchemaContext parseYangSource(final String yangSourcePath) throws SourceException, ReactorException,
            FileNotFoundException, URISyntaxException {
        return parseYangSource(yangSourcePath, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSource(final String yangSourcePath, final StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {
        final URL source = StmtTestUtils.class.getResource(yangSourcePath);
        final File sourceFile = new File(source.toURI());
        return parseYangSources(statementParserMode, sourceFile);
    }

    public static SchemaContext parseYinSources(String yinSourcesDirectoryPath, StatementParserMode statementParserMode)
            throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {

        URL resourceDir = StmtTestUtils.class.getResource(yinSourcesDirectoryPath);
        File testSourcesDir = new File(resourceDir.toURI());

        return parseYinSources(statementParserMode, testSourcesDir.listFiles(YIN_FILE_FILTER));
    }

    public static SchemaContext parseYinSources(StatementParserMode statementParserMode, File... files) throws SourceException,
            ReactorException, FileNotFoundException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YinStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYinSources(statementParserMode, sources);
    }

    public static SchemaContext parseYinSources(StatementParserMode statementParserMode, StatementStreamSource... sources)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(statementParserMode);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static Module findImportedModule(SchemaContext context, Module rootModule, String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        Set<ModuleImport> rootImports = rootModule.getImports();
        for (ModuleImport moduleImport : rootImports) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        Module importedModule = context.findModuleByName(requestedModuleImport.getModuleName(),
                requestedModuleImport.getRevision());
        return importedModule;
    }
}
