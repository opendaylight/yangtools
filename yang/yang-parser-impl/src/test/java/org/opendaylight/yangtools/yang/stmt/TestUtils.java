/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class TestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    private TestUtils() {
    }

    public static SchemaContext loadModules(final URI resourceDirectory)
            throws ReactorException, IOException, YangSyntaxErrorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        File[] files = new File(resourceDirectory).listFiles();

        for (File file : files) {
            if (file.getName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
                reactor.addSource(YangStatementStreamSource.create(YangTextSchemaSource.forFile(file)));
            } else {
                LOG.info("Ignoring non-yang file {}", file);
            }
        }

        return reactor.buildEffective();
    }

    public static SchemaContext loadModuleResources(final Class<?> refClass, final String... resourceNames)
            throws IOException, ReactorException, YangSyntaxErrorException {
        final BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        for (String resourceName : resourceNames) {
            reactor.addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource(refClass, resourceName)));
        }

        return reactor.buildEffective();
    }

    public static SchemaContext loadYinModules(final URI resourceDirectory) throws ReactorException, SAXException,
            IOException {
        final BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        for (File file : new File(resourceDirectory).listFiles()) {
            reactor.addSource(YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                YinTextSchemaSource.forFile(file))));
        }

        return reactor.buildEffective();
    }

    public static Module loadYinModule(final YinTextSchemaSource source) throws ReactorException, SAXException, IOException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(source)));
        SchemaContext ctx = reactor.buildEffective();
        return ctx.getModules().iterator().next();
    }

    public static Optional<Module> findModule(final SchemaContext context, final String moduleName) {
        return context.getModules().stream().filter(module -> moduleName.equals(module.getName())).findAny();
    }

    public static ModuleImport findImport(final Set<ModuleImport> imports, final String prefix) {
        ModuleImport result = null;
        for (ModuleImport moduleImport : imports) {
            if (moduleImport.getPrefix().equals(prefix)) {
                result = moduleImport;
                break;
            }
        }
        return result;
    }

    public static TypeDefinition<?> findTypedef(final Set<TypeDefinition<?>> typedefs, final String name) {
        TypeDefinition<?> result = null;
        for (TypeDefinition<?> td : typedefs) {
            if (td.getQName().getLocalName().equals(name)) {
                result = td;
                break;
            }
        }
        return result;
    }

    public static SchemaPath createPath(final boolean absolute, final URI namespace, final Date revision,
            final String prefix, final String... names) {
        List<QName> path = new ArrayList<>();
        for (String name : names) {
            path.add(QName.create(namespace, revision, name));
        }
        return SchemaPath.create(path, absolute);
    }

    public static Date createDate(final String date) {
        Date result;
        final DateFormat simpleDateFormat = SimpleDateFormatUtil.getRevisionFormat();
        try {
            result = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            result = null;
        }
        return result;
    }

    /**
     * Test if node has augmenting flag set to expected value. In case this is
     * DataNodeContainer/ChoiceNode, check its child nodes/case nodes too.
     *
     * @param node
     *            node to check
     * @param expected
     *            expected value
     */
    public static void checkIsAugmenting(final DataSchemaNode node, final boolean expected) {
        assertEquals(expected, node.isAugmenting());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node)
                    .getChildNodes()) {
                checkIsAugmenting(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                checkIsAugmenting(caseNode, expected);
            }
        }
    }

    /**
     * Check if node has addedByUses flag set to expected value. In case this is
     * DataNodeContainer/ChoiceNode, check its child nodes/case nodes too.
     *
     * @param node
     *            node to check
     * @param expected
     *            expected value
     */
    public static void checkIsAddedByUses(final DataSchemaNode node, final boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node)
                    .getChildNodes()) {
                checkIsAddedByUses(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                checkIsAddedByUses(caseNode, expected);
            }
        }
    }

    public static void checkIsAddedByUses(final GroupingDefinition node, final boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        for (DataSchemaNode child : node.getChildNodes()) {
            checkIsAddedByUses(child, expected);
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

    public static SchemaContext parseYangSources(final StatementStreamSource... sources) throws SourceException,
            ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(final File... files)
            throws SourceException, ReactorException, IOException, YangSyntaxErrorException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = YangStatementStreamSource.create(YangTextSchemaSource.forFile(files[i]));
        }

        return parseYangSources(sources);
    }

    public static SchemaContext parseYangSources(final Collection<File> files)
            throws SourceException, ReactorException, IOException, YangSyntaxErrorException {
        return parseYangSources(files.toArray(new File[files.size()]));
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath)
            throws SourceException, ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {

        URL resourceDir = StmtTestUtils.class.getResource(yangSourcesDirectoryPath);
        File testSourcesDir = new File(resourceDir.toURI());

        return parseYangSources(testSourcesDir.listFiles());
    }

    public static SchemaContext parseYangSource(final String yangSourceFilePath)
            throws SourceException, ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {

        URL resourceFile = StmtTestUtils.class.getResource(yangSourceFilePath);
        File testSourcesFile = new File(resourceFile.toURI());

        return parseYangSources(testSourcesFile);
    }
}
