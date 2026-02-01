/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.FileYinTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYinTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public final class TestUtils {
    private static final @NonNull YinTextToDOMSourceTransformer TEXT_TO_DOM =
        ServiceLoader.load(YinTextToDOMSourceTransformer.class).findFirst().orElseThrow();
    private static final @NonNull YangTextToIRSourceTransformer TEXT_TO_IR =
        ServiceLoader.load(YangTextToIRSourceTransformer.class).findFirst().orElseThrow();

    private TestUtils() {
        // Hidden on purpose
    }

    public static @NonNull List<YangIRSource> loadSources(final String resourceDirectory)
            throws Exception {
        return loadSources(TestUtils.class, resourceDirectory);
    }

    public static @NonNull List<YangIRSource> loadSources(final Class<?> cls, final String resourceDirectory)
            throws Exception {
        final var files = Path.of(cls.getResource(resourceDirectory).toURI())
            // FIXME: use Files instead
            .toFile().listFiles(StmtTestUtils.YANG_FILE_FILTER);

        final var sources = new ArrayList<YangIRSource>(files.length);
        for (var file : files) {
            sources.add(assertYangSource(file.toPath()));
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
        final var action = RFC7950Reactors.defaultReactor().newBuild();
        for (var source : loadSources(cls, resourceDirectory)) {
            action.addSource(source);
        }
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
            reactor.addSource(assertYangSource(resourcePath));
        }
        if (supportedFeatures != null) {
            reactor.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }
        return reactor.buildEffective();
    }

    public static @NonNull YangIRSource assertYangSource(final String resourcePath)
            throws IOException, SourceSyntaxException {
        return TEXT_TO_IR.transformSource(new URLYangTextSource(TestUtils.class.getResource(resourcePath)));
    }

    public static @NonNull YangIRSource assertYangSource(final Path file) throws IOException, SourceSyntaxException {
        return TEXT_TO_IR.transformSource(new FileYangTextSource(file));
    }

    public static @NonNull YinDOMSource assertYinSource(final String resourcePath)
            throws IOException, SourceSyntaxException {
        return TEXT_TO_DOM.transformSource(new URLYinTextSource(TestUtils.class.getResource(resourcePath)));
    }

    public static @NonNull YinDOMSource assertYinSource(final Path file) throws IOException, SourceSyntaxException {
        return TEXT_TO_DOM.transformSource(new FileYinTextSource(file));
    }

    // FIXME: these remain unaudited

    public static EffectiveModelContext loadYinModules(final URI resourceDirectory)
            throws IOException, ReactorException, SourceSyntaxException {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild();

        // FIXME: use Files to list files
        for (var file : Path.of(resourceDirectory).toFile().listFiles()) {
            reactor.addSource(assertYinSource(file.toPath()));
        }

        return reactor.buildEffective();
    }

    public static Module loadYinModule(final YinTextSource source)
            throws IOException, ReactorException, SourceSyntaxException {
        return RFC7950Reactors.defaultReactor().newBuild().addYinSource(TEXT_TO_DOM, source).buildEffective()
            .getModules().iterator().next();
    }

    public static ModuleImport findImport(final Collection<? extends ModuleImport> imports, final String prefix) {
        for (var moduleImport : imports) {
            if (moduleImport.getPrefix().equals(prefix)) {
                return moduleImport;
            }
        }
        return null;
    }

    public static TypeDefinition<?> findTypedef(final Collection<? extends TypeDefinition<?>> typedefs,
            final String name) {
        for (var td : typedefs) {
            if (td.getQName().getLocalName().equals(name)) {
                return td;
            }
        }
        return null;
    }
}
