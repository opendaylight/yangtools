/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

final class ModuleNamespaceContext implements NamespaceContext {
    @NonNullByDefault
    record PrefixAndNamespace(String prefix, XMLNamespace namespace) {
        static final Comparator<PrefixAndNamespace> COMPARATOR = (o1, o2) -> {
            final var cmp = o1.prefix().compareTo(o2.prefix());
            return cmp != 0 ? cmp : o1.namespace().compareTo(o2.namespace());
        };

        PrefixAndNamespace {
            requireNonNull(prefix);
            requireNonNull(namespace);
        }

        PrefixAndNamespace(final ImportEffectiveStatement stmt) {
            this(stmt.prefixArgument(), stmt.importedModule().namespaceArgument());
        }

        PrefixAndNamespace(final ModuleEffectiveStatement stmt) {
            this(stmt.prefixArgument(), stmt.namespaceArgument());
        }
    }

    private static final Entry<String, String> YIN_PREFIX_AND_NAMESPACE =
        Map.entry(XMLConstants.DEFAULT_NS_PREFIX, YangConstants.RFC6020_YIN_NAMESPACE_STRING);

    private final ListMultimap<@NonNull String, @NonNull String> namespaceToPrefix;
    private final @NonNull ModuleEffectiveStatement module;

    ModuleNamespaceContext(final ModuleEffectiveStatement module) {
        this.module = requireNonNull(module);

        final var namespaces = ImmutableListMultimap.<String, String>builder();
        for (var entry : module.namespacePrefixes()) {
            namespaces.put(entry.getKey().namespace().toString(), entry.getValue());
        }
        namespaceToPrefix = namespaces.build();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        checkArgument(prefix != null);

        return switch (prefix) {
            case XMLConstants.DEFAULT_NS_PREFIX -> YangConstants.RFC6020_YIN_NAMESPACE_STRING;
            case XMLConstants.XML_NS_PREFIX -> XMLConstants.XML_NS_URI;
            case XMLConstants.XMLNS_ATTRIBUTE -> XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            default -> {
                if (prefix.equals(module.prefixArgument())) {
                    yield module.namespaceArgument().toString();
                }
                for (var stmt : module.importStatements()) {
                    if (prefix.equals(stmt.prefixArgument())) {
                        yield stmt.importedModule().namespaceArgument().toString();
                    }
                }
                yield XMLConstants.NULL_NS_URI;
            }
        };
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        checkArgument(namespaceURI != null);

        return switch (namespaceURI) {
            case YangConstants.RFC6020_YIN_NAMESPACE_STRING -> XMLConstants.DEFAULT_NS_PREFIX;
            case XMLConstants.XML_NS_URI -> XMLConstants.XML_NS_PREFIX;
            case XMLConstants.XMLNS_ATTRIBUTE_NS_URI -> XMLConstants.XMLNS_ATTRIBUTE;
            default -> {
                final var prefixes = namespaceToPrefix.get(namespaceURI);
                yield prefixes.isEmpty() ? null : prefixes.getFirst();
            }
        };
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        checkArgument(namespaceURI != null);

        return switch (namespaceURI) {
            case YangConstants.RFC6020_YIN_NAMESPACE_STRING ->
                Iterators.singletonIterator(XMLConstants.DEFAULT_NS_PREFIX);
            case XMLConstants.XML_NS_URI -> Iterators.singletonIterator(XMLConstants.XML_NS_PREFIX);
            case XMLConstants.XMLNS_ATTRIBUTE_NS_URI -> Iterators.singletonIterator(XMLConstants.XMLNS_ATTRIBUTE);
            default -> namespaceToPrefix.get(namespaceURI).iterator();
        };
    }

    Entry<String, String> prefixAndNamespaceFor(final QNameModule namespace) {
        if (namespace.equals(YangConstants.RFC6020_YIN_MODULE)) {
            return YIN_PREFIX_AND_NAMESPACE;
        }

        final String prefix = module.findNamespacePrefix(namespace)
            .orElseThrow(() -> new IllegalArgumentException("Module " + namespace + " does not map to a prefix"));
        return Map.entry(prefix, namespace.namespace().toString());
    }

    @NonNullByDefault
    Iterator<PrefixAndNamespace> prefixToNamespaceIterator() {
        final var list = new ArrayList<PrefixAndNamespace>();
        list.add(new PrefixAndNamespace(module));
        for (var stmt : module.importStatements()) {
            list.add(new PrefixAndNamespace(stmt));
        }
        list.sort(PrefixAndNamespace.COMPARATOR);
        return list.iterator();
    }
}
