/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReferenceAware;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RootDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RootEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.spi.source.MaterializedSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Statement stream source, which is used for inference of effective model.
 *
 * <p>Statement stream source is required to emit its statements using supplied {@link StatementWriter}.
 *
 * <p>Since YANG allows language extensions defined in sources (which defines how source is serialized), instances of
 * extensions present anywhere and forward references, each source needs to be processed in three steps, where each step
 * uses different set of supported statements.
 *
 * <p>Steps (in order of invocation) are:
 * <ol>
 * <li>{@link #writeLinkage(StatementWriter, StatementDefinitionResolver)} -
 * Source MUST emit only statements related in linkage, which are present in
 * supplied statement definition map. This step is used to build cross-source
 * linkage and visibility relationship, and to determine XMl namespaces and
 * prefixes.</li>
 * <li>
 * {@link #writeLinkageAndStatementDefinitions(StatementWriter, StatementDefinitionResolver)}
 * - Source MUST emit only statements related to linkage and language extensions
 * definitions, which are present in supplied statement definition map. This
 * step is used to build statement definitions in order to fully processed
 * source.</li>
 * <li>
 * {@link #writeFull(StatementWriter, StatementDefinitionResolver)}
 * - Source MUST emit all statements present in source. This step is used to
 * build full declared statement model of source.</li>
 * </ol>
 */
// FIXME: 7.0.0: this is a push parser, essentially traversing the same tree multiple times. Perhaps we should create
//               a visitor/filter or perform some explicit argument binding?
public sealed interface StatementStreamSource permits YangIRStatementStreamSource, YinDOMStatementStreamSource {
    /**
     * A factory for {@link StatementStreamSource}s.
     */
    @NonNullByDefault
    @FunctionalInterface
    interface Factory {
        /**
         * {@return a new {@link StatementStreamSource} backed by specified prefix mapping}
         * @param prefixToModule the prefix mapping
         */
        StatementStreamSource newStreamSource(Map<? extends Unqualified, ? extends QNameModule> prefixToModule);
    }

    /**
     * A factory factory for {@link StatementStreamSource}s.
     *
     * @param <S> the type of {@link SourceRepresentation}
     */
    @NonNullByDefault
    @FunctionalInterface
    interface Support<S extends MaterializedSourceRepresentation<?, ?>> {
        /**
         * {@return a new {@link Factory} backed by specified source and version}
         * @param source the source
         * @param yangVersion the version
         */
        Factory newFactory(S source, YangVersion yangVersion);
    }

    /**
     * Bare-minimum information about the root of a {@link StatementStreamSource}.
     */
    sealed interface Root extends Immutable, StatementSourceReferenceAware {
        /**
         * {@return the statement definition}
         */
        @NonNull StatementDefinition<Unqualified, ? extends RootDeclaredStatement, ? extends RootEffectiveStatement<?>>
            definition();

        /**
         * {@return the statement argument}
         */
        @NonNull String rawArgument();

        /**
         * {@return the number of substatements}
         */
        int size();
    }

    /**
     * A {@code module} {@link Root}.
     */
    record ModuleRoot(
            @NonNull StatementSourceReference sourceRef,
            @NonNull String rawArgument,
            int size) implements Root {
        public ModuleRoot {
            requireNonNull(sourceRef);
            requireNonNull(rawArgument);
            checkArgument(size > 0);
        }

        @Override
        public StatementDefinition<Unqualified, ModuleStatement, ModuleEffectiveStatement> definition() {
            return ModuleStatement.DEF;
        }
    }

    /**
     * A {@code submodule} {@link Root}.
     */
    record SubmoduleRoot(
            @NonNull StatementSourceReference sourceRef,
            @NonNull String rawArgument,
            int size) implements Root {
        public SubmoduleRoot {
            requireNonNull(sourceRef);
            requireNonNull(rawArgument);
            checkArgument(size > 0);
        }

        @Override
        public StatementDefinition<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement> definition() {
            return SubmoduleStatement.DEF;
        }
    }

    /**
     * Bare-minimum information about the root of a {@link StatementStreamSource}.
     *
     * @param <D> declared statement type
     * @param <E> effective statement type
     * @param definition the {@link StatementDefinition}
     * @param argument the statement argument
     * @param size the number of substatements
     */
    record RootStatement<D extends RootDeclaredStatement, E extends RootEffectiveStatement<D>>(
            @NonNull StatementDefinition<Unqualified, D, E> definition,
            @NonNull Unqualified argument,
            int size) implements Immutable {
        public RootStatement {
            requireNonNull(definition);
            requireNonNull(argument);
        }
    }

    /**
     * The {@link Factory} for {@link YangIRSource}.
     */
    @NonNullByDefault
    static Support<YangIRSource> forYangIR() {
        return YangIRStatementStreamSource.SUPPORT;
    }

    /**
     * The {@link Factory} for {@link YinDOMSource}.
     */
    @NonNullByDefault
    static Support<YinDOMSource> forYInDOM() {
        return YinDOMStatementStreamSource.SUPPORT;
    }

    /**
     * {@return the {@link Root}}
     */
    @NonNull Root root();

    /**
     * Emits only linkage-related statements to supplied {@code writer} based on specified YANG version.
     * Default implementation does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param resolver
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and
     *            MUST NOT emit any other statements.
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkage(StatementWriter writer, StatementDefinitionResolver resolver);

    /**
     * Emits only linkage and language extension statements to supplied
     * {@code writer} based on specified YANG version. Default implementation
     * does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param resolver
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and
     *            MUST NOT emit any other statements.
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkageAndStatementDefinitions(StatementWriter writer, StatementDefinitionResolver resolver);

    /**
     * Emits every statements present in this statement source to supplied
     * {@code writer} based on specified yang version. Default implementation
     * does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param resolver
     *            Map of available statement definitions.
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeFull(StatementWriter writer, StatementDefinitionResolver resolver);
}
