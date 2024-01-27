/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

final class QNameFactory {
    private static final class StringQName implements Immutable {
        private final @NonNull String localName;
        private final @NonNull String namespace;
        private final @Nullable String revision;

        StringQName(final String localName, final String namespace, final String revision) {
            this.localName = requireNonNull(localName);
            this.namespace = requireNonNull(namespace);
            this.revision = revision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(localName, namespace, revision);
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof StringQName other && localName.equals(other.localName)
                && namespace.equals(other.namespace) && Objects.equals(revision, other.revision);
        }

        QName toQName() {
            return revision != null ? QName.create(namespace, revision, localName) : QName.create(namespace, localName);
        }
    }

    private static final class ModuleQName implements Immutable {
        private final @NonNull QNameModule module;
        private final @NonNull String localName;

        ModuleQName(final QNameModule module, final String localName) {
            this.module = requireNonNull(module);
            this.localName = requireNonNull(localName);
        }

        @Override
        public int hashCode() {
            return 31 * module.hashCode() + localName.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof ModuleQName other && localName.equals(other.localName)
                && module.equals(other.module);
        }

        QName toQName() {
            return QName.create(module, localName);
        }
    }

    private static final class StringModule implements Immutable {
        private final @NonNull String namespace;
        private final @Nullable String revision;

        StringModule(final String namespace, final String revision) {
            this.namespace = requireNonNull(namespace);
            this.revision = revision;
        }

        @Override
        public int hashCode() {
            return 31 * namespace.hashCode() + Objects.hashCode(revision);
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof StringModule other && namespace.equals(other.namespace)
                && Objects.equals(revision, other.revision);
        }

        QNameModule toQNameModule() {
            return QNameModule.ofRevision(namespace, revision);
        }
    }

    private static final int MAX_QNAME_CACHE_SIZE = Integer.getInteger(
        "org.opendaylight.controller.cluster.datastore.node.utils.qname-cache.max-size", 10000);
    private static final int MAX_MODULE_CACHE_SIZE = Integer.getInteger(
        "org.opendaylight.controller.cluster.datastore.node.utils.module-cache.max-size", 2000);

    private static final LoadingCache<StringQName, QName> STRING_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_QNAME_CACHE_SIZE).weakValues().build(new CacheLoader<StringQName, QName>() {
                @Override
                public QName load(final StringQName key) {
                    return key.toQName().intern();
                }
            });
    private static final LoadingCache<ModuleQName, QName> QNAME_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_QNAME_CACHE_SIZE).weakValues().build(new CacheLoader<ModuleQName, QName>() {
                @Override
                public QName load(final ModuleQName key) {
                    return key.toQName().intern();
                }
            });
    private static final LoadingCache<StringModule, QNameModule> MODULE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_MODULE_CACHE_SIZE).weakValues().build(new CacheLoader<StringModule, QNameModule>() {
                @Override
                public QNameModule load(final StringModule key) {
                    return key.toQNameModule().intern();
                }
            });
    private static final LoadingCache<ModuleQName, NodeIdentifier> NODEID_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_QNAME_CACHE_SIZE).weakValues().build(new CacheLoader<ModuleQName, NodeIdentifier>() {
                @Override
                public NodeIdentifier load(final ModuleQName key) throws ExecutionException {
                    return NodeIdentifier.create(QNAME_CACHE.get(key));
                }
            });

    private QNameFactory() {

    }

    static QName create(final String localName, final String namespace, final @Nullable String revision) {
        return STRING_CACHE.getUnchecked(new StringQName(localName, namespace, revision));
    }

    static QName create(final QNameModule module, final String localName) {
        return QNAME_CACHE.getUnchecked(new ModuleQName(module, localName));
    }

    static QNameModule createModule(final String namespace, final @Nullable String revision) {
        return MODULE_CACHE.getUnchecked(new StringModule(namespace, revision));
    }

    static @NonNull NodeIdentifier getNodeIdentifier(final QNameModule module, final String localName)
            throws ExecutionException {
        return NODEID_CACHE.get(new ModuleQName(module, localName));
    }
}
