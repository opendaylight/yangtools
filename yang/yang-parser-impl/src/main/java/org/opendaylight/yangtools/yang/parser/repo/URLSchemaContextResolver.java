/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Function;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformationException;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.impl.YangParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
public class URLSchemaContextResolver {
    private static final Logger LOG = LoggerFactory.getLogger(URLSchemaContextResolver.class);
    private static final Function<ASTSchemaSource, YangModelDependencyInfo> EXTRACT_DEPINFO = new Function<ASTSchemaSource, YangModelDependencyInfo>() {
        @Override
        public YangModelDependencyInfo apply(final ASTSchemaSource input) {
            return input.getDependencyInformation();
        }
    };
    private static final EntryTransformer<SourceIdentifier, Collection<YangModelDependencyInfo>, YangModelDependencyInfo> SQUASH_DEPINFO =
            new EntryTransformer<SourceIdentifier, Collection<YangModelDependencyInfo>, YangModelDependencyInfo>() {
        @Override
        public YangModelDependencyInfo transformEntry(final SourceIdentifier key, final Collection<YangModelDependencyInfo> value) {
            // FIXME: validate that all the info objects are the same
            return value.iterator().next();
        }
    };
    private static final Function<ASTSchemaSource, ParserRuleContext> EXTRACT_AST = new Function<ASTSchemaSource, ParserRuleContext>() {
        @Override
        public ParserRuleContext apply(final ASTSchemaSource input) {
            return input.getAST();
        }
    };
    private static final EntryTransformer<SourceIdentifier, Collection<ParserRuleContext>, ParserRuleContext> SQUASH_AST =
            new EntryTransformer<SourceIdentifier, Collection<ParserRuleContext>, ParserRuleContext>() {
        @Override
        public ParserRuleContext transformEntry(final SourceIdentifier key, final Collection<ParserRuleContext> value) {
            // FIXME: validate that all the info objects are the same
            return value.iterator().next();
        }
    };

    @GuardedBy("this")
    private final Multimap<SourceIdentifier, ASTSchemaSource> resolvedRegs = ArrayListMultimap.create();
    private final AtomicReference<Optional<SchemaContext>> currentSchemaContext = new AtomicReference<>(Optional.<SchemaContext>absent());
    private final Queue<URLRegistration> outstandingRegs = new ConcurrentLinkedQueue<>();
    private final TextToASTTransformer transformer;
    @GuardedBy("this")
    private Object version = new Object();
    @GuardedBy("this")
    private Object contextVersion = version;

    private final class URLRegistration extends AbstractObjectRegistration<URL> {
        @GuardedBy("this")
        private CheckedFuture<ASTSchemaSource, SchemaSourceTransformationException> future;
        @GuardedBy("this")
        private ASTSchemaSource result;

        protected URLRegistration(final URL url, final CheckedFuture<ASTSchemaSource, SchemaSourceTransformationException> future) {
            super(url);
            this.future = Preconditions.checkNotNull(future);
        }

        private synchronized boolean setResult(final ASTSchemaSource result) {
            if (future != null) {
                this.result = result;
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void removeRegistration() {
            // Cancel the future, but it may already be completing
            future.cancel(false);

            synchronized (this) {
                future = null;
                outstandingRegs.remove(this);
                if (result != null) {
                    removeSchemaSource(result);
                }
            }
        }
    }

    URLSchemaContextResolver(final TextToASTTransformer transformer) {
        this.transformer = Preconditions.checkNotNull(transformer);
    }

    /**
     * Register a URL hosting a YANG Text file.
     *
     * @param url URL
     */
    public ObjectRegistration<URL> registerSource(final URL url) {
        checkArgument(url != null, "Supplied URL must not be null");

        final SourceIdentifier id = SourceIdentifier.create(url.getFile().toString(), Optional.<String>absent());
        final YangTextSchemaSource text = new YangTextSchemaSource(id) {
            @Override
            public InputStream openStream() throws IOException {
                return url.openStream();
            }

            @Override
            protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                return toStringHelper.add("url", url);
            }
        };

        final CheckedFuture<ASTSchemaSource, SchemaSourceTransformationException> ast = transformer.transformSchemaSource(text);
        final URLRegistration reg = new URLRegistration(url, ast);
        outstandingRegs.add(reg);

        Futures.addCallback(ast, new FutureCallback<ASTSchemaSource>() {
            @Override
            public void onSuccess(final ASTSchemaSource result) {
                LOG.trace("Resolved URL {} to source {}", url, result);

                outstandingRegs.remove(reg);
                if (reg.setResult(result)) {
                    addSchemaSource(result);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Failed to parse YANG text from {}, ignoring it", url, t);
                outstandingRegs.remove(reg);
            }
        });

        return reg;
    }

    private synchronized void addSchemaSource(final ASTSchemaSource src) {
        resolvedRegs.put(src.getIdentifier(), src);
        version = new Object();
    }

    private synchronized void removeSchemaSource(final ASTSchemaSource src) {
        resolvedRegs.put(src.getIdentifier(), src);
        version = new Object();
    }

    /**
     * Try to parse all currently available yang files and build new schema context.
     * @return new schema context iif there is at least 1 yang file registered and new schema context was successfully built.
     */
    public Optional<SchemaContext> getSchemaContext() {
        while (true) {
            Optional<SchemaContext> result;
            final Multimap<SourceIdentifier, ASTSchemaSource> sources;
            final Object v;
            synchronized (this) {
                result = currentSchemaContext.get();
                if (version == contextVersion) {
                    return result;
                }

                sources = ImmutableMultimap.copyOf(resolvedRegs);
                v = version;
            }

            if (!sources.isEmpty()) {
                final Map<SourceIdentifier, YangModelDependencyInfo> deps =
                        Maps.transformEntries(Multimaps.transformValues(sources, EXTRACT_DEPINFO).asMap(), SQUASH_DEPINFO);

                LOG.debug("Resolving dependency reactor {}", deps);
                final DependencyResolver res = DependencyResolver.create(deps);
                if (!res.getUnresolvedSources().isEmpty()) {
                    LOG.debug("Omitting models {} due to unsatisfied imports {}", res.getUnresolvedSources(), res.getUnsatisfiedImports());
                }

                final Map<SourceIdentifier, ParserRuleContext> asts =
                        Maps.transformEntries(Multimaps.transformValues(sources, EXTRACT_AST).asMap(), SQUASH_AST);

                final ParseTreeWalker walker = new ParseTreeWalker();
                final Map<SourceIdentifier, ModuleBuilder> sourceToBuilder = new LinkedHashMap<>();

                for (Entry<SourceIdentifier, ParserRuleContext> entry : asts.entrySet()) {
                    final YangParserListenerImpl yangModelParser = new YangParserListenerImpl(entry.getKey().getName());
                    walker.walk(yangModelParser, entry.getValue());
                    ModuleBuilder moduleBuilder = yangModelParser.getModuleBuilder();

                    // FIXME: do we need to lug this around?
                    // moduleBuilder.setSource(source);
                    sourceToBuilder.put(entry.getKey(), moduleBuilder);
                }
                LOG.debug("Modules ready for integration");

                final YangParserImpl parser = YangParserImpl.getInstance();
                final Collection<Module> modules = parser.buildModules(sourceToBuilder.values());
                LOG.debug("Integrated cross-references modules");

                result = Optional.of(parser.assembleContext(modules));
            } else {
                result = Optional.absent();
            }

            synchronized (this) {
                if (v == version) {
                    currentSchemaContext.set(result);
                    contextVersion = version;
                    return result;
                }

                LOG.debug("Context version {} expected {}, retry", version, v);
            }
        }
    }
}
