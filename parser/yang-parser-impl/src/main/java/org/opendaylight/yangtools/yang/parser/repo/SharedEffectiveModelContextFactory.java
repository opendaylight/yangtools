/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An almost-simple cache. EffectiveModel computation is explicitly asynchronous and we are also threadless, i.e. we
 * hijack repository threads to do our work.
 */
final class SharedEffectiveModelContextFactory implements EffectiveModelContextFactory {
    private static final class CacheEntry {
        private static final Function<EffectiveModelContext, Reference<EffectiveModelContext>> REF;
        private static final VarHandle STATE;

        static {
            try {
                STATE = MethodHandles.lookup().findVarHandle(CacheEntry.class, "state", Object.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }

            String prop = System.getProperty("org.opendaylight.yangtools.yang.parser.repo.shared-refs", "weak");
            REF = switch (prop) {
                case "soft" -> SoftReference::new;
                case "weak" -> WeakReference::new;
                default -> {
                    LOG.warn("Invalid shared-refs \"{}\", defaulting to weak references", prop);
                    prop = "weak";
                    yield WeakReference::new;
                }
            };
            LOG.info("Using {} references", prop);
        }

        // This field can be in one of two states:
        // - SettableFuture, in which case the model is being computed
        // - Reference, in which case the model is available through the reference (unless cleared)
        @SuppressWarnings("unused")
        @SuppressFBWarnings(value = "URF_UNREAD_FIELD",
            justification = "https://github.com/spotbugs/spotbugs/issues/2749")
        private volatile Object state = SettableFuture.create();

        @SuppressWarnings("unchecked")
        @Nullable ListenableFuture<EffectiveModelContext> future() {
            final Object local = STATE.getAcquire(this);
            if (local instanceof SettableFuture) {
                return (SettableFuture<EffectiveModelContext>) local;
            }
            verify(local instanceof Reference, "Unexpected state %s", local);
            final EffectiveModelContext model = ((Reference<EffectiveModelContext>) local).get();
            return model == null ? null : Futures.immediateFuture(model);
        }

        @SuppressWarnings("unchecked")
        @NonNull SettableFuture<EffectiveModelContext> getFuture() {
            final Object local = STATE.getAcquire(this);
            verify(local instanceof SettableFuture, "Unexpected state %s", local);
            return (SettableFuture<EffectiveModelContext>) local;
        }

        void resolve(final EffectiveModelContext context) {
            final SettableFuture<EffectiveModelContext> future = getFuture();
            // Publish a weak reference before triggering any listeners on the future so that newcomers can see it
            final Object witness = STATE.compareAndExchangeRelease(this, future, REF.apply(context));
            verify(witness == future, "Unexpected witness %s", witness);
            future.set(context);
        }
    }


    private static final Logger LOG = LoggerFactory.getLogger(SharedEffectiveModelContextFactory.class);
    private static final Cleaner CLEANER = Cleaner.create();

    private final ConcurrentMap<Set<SourceIdentifier>, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AssembleSources assembleSources;
    private final SchemaRepository repository;

    SharedEffectiveModelContextFactory(final @NonNull SharedSchemaRepository repository,
            final @NonNull SchemaContextFactoryConfiguration config) {
        this.repository = requireNonNull(repository);
        assembleSources = new AssembleSources(repository.factory(), config);

    }

    @Override
    public @NonNull ListenableFuture<EffectiveModelContext> createEffectiveModelContext(
            final @NonNull Collection<SourceIdentifier> requiredSources) {
        return createEffectiveModel(dedupSources(requiredSources));
    }

    @NonNull ListenableFuture<EffectiveModelContext> createEffectiveModel(final Set<SourceIdentifier> sources) {
        final CacheEntry existing = cache.get(sources);
        return existing != null ? acquireModel(sources, existing) : computeModel(sources);
    }

    // We may have an entry, but we do not know in what state it is in: it may be stable, it may be being built up
    // or in process of being retired.
    private @NonNull ListenableFuture<EffectiveModelContext> acquireModel(final Set<SourceIdentifier> sources,
            final @NonNull CacheEntry entry) {
        // Request a future from the entry, which indicates the context is either available or being constructed
        final ListenableFuture<EffectiveModelContext> existing = entry.future();
        if (existing != null) {
            return existing;
        }
        // The entry cannot satisfy our request: remove it and fall back to computation
        cache.remove(sources, entry);
        return computeModel(sources);
    }

    private @NonNull ListenableFuture<EffectiveModelContext> computeModel(final Set<SourceIdentifier> sources) {
        // Insert a new entry until we succeed or there is a workable entry
        final CacheEntry ourEntry = new CacheEntry();
        while (true) {
            final CacheEntry prevEntry = cache.putIfAbsent(sources, ourEntry);
            if (prevEntry == null) {
                // successful insert
                break;
            }

            // ... okay, we have raced, but is the entry still usable?
            final ListenableFuture<EffectiveModelContext> existing = prevEntry.future();
            if (existing != null) {
                // .. yup, we are done here
                return existing;
            }

            // ... no dice, remove the entry and retry
            cache.remove(sources, prevEntry);
        }

        // Acquire the future first, then kick off computation. That way we do not need to worry about races around
        // EffectiveModelContext being garbage-collected just after have computed it and before we have acquired a
        // reference to it.
        final ListenableFuture<EffectiveModelContext> result = ourEntry.getFuture();
        resolveEntry(sources, ourEntry);
        return result;
    }

    private void resolveEntry(final Set<SourceIdentifier> sources, final CacheEntry entry) {
        LOG.debug("Starting assembly of {} sources", sources.size());
        final Stopwatch sw = Stopwatch.createStarted();

        // Request all sources be loaded
        ListenableFuture<List<YangIRSchemaSource>> sf = Futures.allAsList(Collections2.transform(sources,
            identifier -> repository.getSchemaSource(identifier, YangIRSchemaSource.class)));

        // Detect mismatch between requested Source IDs and IDs that are extracted from parsed source
        // Also remove duplicates if present
        // We are relying on preserved order of uniqueSourceIdentifiers as well as sf
        sf = Futures.transform(sf, new SourceIdMismatchDetector(sources), MoreExecutors.directExecutor());

        // Assemble sources into a schema context
        final ListenableFuture<EffectiveModelContext> cf = Futures.transformAsync(sf, assembleSources,
            MoreExecutors.directExecutor());

        // FIXME: we do not deal with invalidation here. We should monitor the repository for changes in source schemas
        //        and react appropriately:
        //        - in case we failed certainly want to invalidate the entry
        //        - in case of success ... that's something to consider
        Futures.addCallback(cf, new FutureCallback<>() {
            @Override
            public void onSuccess(final EffectiveModelContext result) {
                LOG.debug("Finished assembly of {} sources in {}", sources.size(), sw);

                // Remove the entry when the context is GC'd
                final Stopwatch residence = Stopwatch.createStarted();
                CLEANER.register(result, () -> {
                    LOG.debug("Removing entry after {}", residence);
                    cache.remove(sources, entry);
                });

                // Flip the entry to resolved
                entry.resolve(result);
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.debug("Failed assembly of {} in {}", sources, sw, cause);
                entry.getFuture().setException(cause);

                // remove failed result from the cache so it can be recomputed, as this might have been a transient
                // problem.
                cache.remove(sources, entry);
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Return a set of de-duplicated inputs.
     *
     * @return set (preserving ordering) from the input collection
     */
    private static ImmutableSet<SourceIdentifier> dedupSources(final Collection<SourceIdentifier> sources) {
        final ImmutableSet<SourceIdentifier> result = ImmutableSet.copyOf(sources);
        if (result.size() != sources.size()) {
            LOG.warn("Duplicate sources requested for schema context, removed duplicate sources: {}",
                Collections2.filter(result, input -> Iterables.frequency(sources, input) > 1));
        }
        return result;
    }
}
