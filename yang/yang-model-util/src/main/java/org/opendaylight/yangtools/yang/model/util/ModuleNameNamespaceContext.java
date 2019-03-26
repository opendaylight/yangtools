/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * Utility {@link YangNamespaceContext} backed by a SchemaContext, resolving namespaces to their module names. This
 * is useful for implementing namespace resolution according to
 * <a href="https://tools.ietf.org/html/rfc7951#section-4">RFC7951 Section 4</a>.
 *
 * <p>
 * When multiple revisions of a particular namespace are present in the backing SchemaContext, this ambiguity is
 * resolved by using the latest revision available.
 *
 * @author Robert Varga
 */
@Beta
public final class ModuleNameNamespaceContext implements YangNamespaceContext, SchemaContextProvider {
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Handled through writeReplace()")
    private final SchemaContext schemaContext;

    public ModuleNameNamespaceContext(final SchemaContext schemaContext) {
        this.schemaContext = requireNonNull(schemaContext);
    }

    /**
     * Convert this object to an equivalent {@link BiMapYangNamespaceContext}.
     *
     * @return A BiMapYangNamespaceContext.
     */
    public BiMapYangNamespaceContext toBiMap() {
        final Builder<String, QNameModule> builder = ImmutableBiMap.builder();
        for (String name : schemaContext.getModules().stream().map(Module::getName).collect(Collectors.toSet())) {
            builder.put(name, findNamespaceForPrefix(name).get());
        }
        return new BiMapYangNamespaceContext(builder.build());
    }

    @Override
    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public Optional<QNameModule> findNamespaceForPrefix(final String prefix) {
        return schemaContext.findModules(prefix).stream().findFirst().map(Module::getQNameModule);
    }

    @Override
    public Optional<String> findPrefixForNamespace(final QNameModule namespace) {
        return schemaContext.findModule(namespace).map(Module::getName);
    }

    private Object writeReplace() {
        return toBiMap();
    }
}
