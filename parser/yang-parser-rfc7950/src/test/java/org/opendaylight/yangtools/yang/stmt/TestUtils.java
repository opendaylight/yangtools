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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
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
        // Hidden on purpose
    }

    public static List<YangTextSchemaSource> listYangSources(final Path path) throws IOException {
        return Files.list(path)
            .filter(child -> {
                if (!child.getFileName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
                    LOG.info("Ignoring non-YANG file {}", child);
                    return false;
                }
                return true;
            })
            .map(YangTextSchemaSource::forPath)
            .collect(Collectors.toUnmodifiableList());
    }

    public static List<YangStatementStreamSource> listYangStreams(final Path path)
            throws IOException, YangSyntaxErrorException {
        final var sources = listYangSources(path);
        final var streams = new ArrayList<YangStatementStreamSource>(sources.size());
        for (var source : sources) {
            streams.add(YangStatementStreamSource.create(source));
        }
        return streams;
    }

    public static EffectiveModelContext loadModules(final String resourceDirectory)
            throws ReactorException, IOException, YangSyntaxErrorException {
        try {
            return loadModules(TestUtils.class.getResource(resourceDirectory).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static EffectiveModelContext loadModules(final URI resourceDirectory)
            throws ReactorException, IOException, YangSyntaxErrorException {
        final BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild();
        listYangStreams(Path.of(resourceDirectory)).forEach(reactor::addSource);
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

    public static EffectiveModelContext parseYangSources(final Path... paths)
            throws ReactorException, IOException, YangSyntaxErrorException {

        StatementStreamSource[] sources = new StatementStreamSource[paths.length];
        for (int i = 0; i < paths.length; i++) {
            sources[i] = YangStatementStreamSource.create(YangTextSchemaSource.forPath(paths[i]));
        }

        return parseYangSources(sources);
    }

    public static EffectiveModelContext parseYangSources(final Collection<Path> paths)
            throws ReactorException, IOException, YangSyntaxErrorException {
        return parseYangSources(paths.toArray(new Path[0]));
    }

    public static EffectiveModelContext parseYangSource(final String yangSourceFilePath)
            throws ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        return parseYangSources(Path.of(StmtTestUtils.class.getResource(yangSourceFilePath).toURI()));
    }
}
