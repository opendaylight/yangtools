/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.FileYinTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.xml.sax.SAXException;

public final class TestUtils {
    private TestUtils() {
        // Hidden on purpose
    }

    public static @NonNull List<StatementStreamSource> loadSources(final String resourceDirectory)
            throws Exception {
        return loadSources(TestUtils.class, resourceDirectory);
    }

    public static @NonNull List<StatementStreamSource> loadSources(final Class<?> cls, final String resourceDirectory)
            throws Exception {
        // FIXME: use Path instead
        final var files = new File(cls.getResource(resourceDirectory).toURI())
            .listFiles(StmtTestUtils.YANG_FILE_FILTER);
        final var sources = new ArrayList<StatementStreamSource>(files.length);
        for (var file : files) {
            sources.add(YangStatementStreamSource.create(new FileYangTextSource(file.toPath())));
        }
        return sources;
    }

    public static EffectiveModelContext loadModules(final String resourceDirectory) throws Exception {
        return loadModules(TestUtils.class, resourceDirectory);
    }

    public static EffectiveModelContext loadModules(final String resourceDirectory,
            final @Nullable Set<QName> supportedFeatures) throws Exception {
        return loadModules(TestUtils.class, resourceDirectory, supportedFeatures);
    }

    public static EffectiveModelContext loadModules(final Class<?> cls, final String resourceDirectory)
            throws Exception {
        return loadModules(cls, resourceDirectory, null);
    }

    public static EffectiveModelContext loadModules(final Class<?> cls, final String resourceDirectory,
            final @Nullable Set<QName> supportedFeatures) throws Exception {
        final var action = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(loadSources(cls, resourceDirectory));
        if (supportedFeatures != null) {
            action.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }
        return action.buildEffective();
    }

    public static EffectiveModelContext parseYangSource(final String... yangSourceFilePath) throws Exception {
        return parseYangSource(List.of(yangSourceFilePath), null);
    }

    public static EffectiveModelContext parseYangSource(final List<String> yangSourceFilePath,
            final @Nullable Set<QName> supportedFeatures) throws Exception {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild();
        for (var resourcePath : yangSourceFilePath) {
            reactor.addSource(YangStatementStreamSource.create(assertSchemaSource(resourcePath)));
        }
        if (supportedFeatures != null) {
            reactor.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }
        return reactor.buildEffective();
    }

    public static YangTextSource assertSchemaSource(final String resourcePath) {
        return new URLYangTextSource(TestUtils.class.getResource(resourcePath));
    }

    // FIXME: these remain unaudited

    public static EffectiveModelContext loadYinModules(final URI resourceDirectory)
            throws ReactorException, SAXException, IOException {
        final BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild();

        for (File file : new File(resourceDirectory).listFiles()) {
            reactor.addSource(YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                new FileYinTextSource(file.toPath()))));
        }

        return reactor.buildEffective();
    }

    public static Module loadYinModule(final YinTextSource source) throws ReactorException, SAXException, IOException {
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
}
