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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class TestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    private TestUtils() {
    }

    public static EffectiveModelContext loadModules(final URI resourceDirectory)
            throws ReactorException, IOException, YangSyntaxErrorException {
        final BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild();
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

    public static EffectiveModelContext loadModuleResources(final Class<?> refClass, final String... resourceNames)
            throws IOException, ReactorException, YangSyntaxErrorException {
        final BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild();

        for (String resourceName : resourceNames) {
            reactor.addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource(refClass,
                resourceName)));
        }

        return reactor.buildEffective();
    }

    public static EffectiveModelContext loadYinModules(final URI resourceDirectory)
            throws ReactorException, SAXException, IOException {
        final BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild();

        for (File file : new File(resourceDirectory).listFiles()) {
            reactor.addSource(YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                YinTextSchemaSource.forFile(file))));
        }

        return reactor.buildEffective();
    }

    public static Module loadYinModule(final YinTextSchemaSource source) throws ReactorException, SAXException,
            IOException {
        return RFC7950Reactors.defaultReactor().newBuild()
                .addSource(YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(source)))
                .buildEffective()
                .getModules().iterator().next();
    }

    @Deprecated(forRemoval = true)
    public static Optional<? extends @NonNull Module> findModule(final SchemaContext context, final String moduleName) {
        return context.getModules().stream().filter(module -> moduleName.equals(module.getName())).findAny();
    }

    public static ModuleImport findImport(final Collection<? extends ModuleImport> imports, final String prefix) {
        for (ModuleImport moduleImport : imports) {
            if (moduleImport.getPrefix().equals(prefix)) {
                return moduleImport;
            }
        }
        return null;
    }

    public static TypeDefinition<?> findTypedef(final Collection<? extends TypeDefinition<?>> typedefs,
            final String name) {
        for (TypeDefinition<?> td : typedefs) {
            if (td.getQName().getLocalName().equals(name)) {
                return td;
            }
        }
        return null;
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
            for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                checkIsAugmenting(caseNode, expected);
            }
        }
    }

    public static List<Module> findModules(final Collection<? extends Module> modules, final String moduleName) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result.add(module);
            }
        }
        return result;
    }

    public static EffectiveModelContext parseYangSources(final StatementStreamSource... sources)
            throws ReactorException {
        return RFC7950Reactors.defaultReactor().newBuild().addSources(sources).buildEffective();
    }

    public static EffectiveModelContext parseYangSources(final File... files)
            throws ReactorException, IOException, YangSyntaxErrorException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = YangStatementStreamSource.create(YangTextSchemaSource.forFile(files[i]));
        }

        return parseYangSources(sources);
    }

    public static EffectiveModelContext parseYangSources(final Collection<File> files)
            throws ReactorException, IOException, YangSyntaxErrorException {
        return parseYangSources(files.toArray(new File[files.size()]));
    }

    public static EffectiveModelContext parseYangSources(final String yangSourcesDirectoryPath)
            throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {

        URL resourceDir = StmtTestUtils.class.getResource(yangSourcesDirectoryPath);
        File testSourcesDir = new File(resourceDir.toURI());

        return parseYangSources(testSourcesDir.listFiles(
            (dir, name) -> name.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)));
    }

    public static EffectiveModelContext parseYangSource(final String yangSourceFilePath)
            throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {

        URL resourceFile = StmtTestUtils.class.getResource(yangSourceFilePath);
        File testSourcesFile = new File(resourceFile.toURI());

        return parseYangSources(testSourcesFile);
    }
}
