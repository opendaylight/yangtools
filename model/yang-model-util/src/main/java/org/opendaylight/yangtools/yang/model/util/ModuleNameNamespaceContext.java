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
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Utility {@link YangNamespaceContext} backed by a SchemaContext, resolving namespaces to their module names. This
 * is useful for implementing namespace resolution according to
 * <a href="https://www.rfc-editor.org/rfc/rfc7951#section-4">RFC7951 Section 4</a>.
 *
 * <p>
 * When multiple revisions of a particular namespace are present in the backing SchemaContext, this ambiguity is
 * resolved by using the latest revision available.
 */
@Beta
public final class ModuleNameNamespaceContext implements YangNamespaceContext {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull EffectiveModelContext modelContext;

    public ModuleNameNamespaceContext(final EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
    }

    /**
     * Convert this object to an equivalent {@link BiMapYangNamespaceContext}.
     *
     * @return A BiMapYangNamespaceContext.
     */
    public BiMapYangNamespaceContext toBiMap() {
        final var builder = ImmutableBiMap.<String, QNameModule>builder();
        for (var module : modelContext.getModuleStatements().values()) {
            final var name = module.argument().getLocalName();
            builder.put(name, findNamespaceForPrefix(name).orElseThrow());
        }
        return new BiMapYangNamespaceContext(builder.build());
    }

    @Override
    public QNameModule namespaceForPrefix(final String prefix) {
        final var modules = modelContext.findModuleStatements(prefix).iterator();
        return modules.hasNext() ? modules.next().localQNameModule() : null;
    }

    @Override
    public String prefixForNamespace(final QNameModule namespace) {
        return modelContext.findModuleStatement(namespace)
            .map(module -> module.argument().getLocalName())
            .orElse(null);
    }

    @java.io.Serial
    private Object writeReplace() {
        return toBiMap();
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throwNSE();
    }

    private static void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(ModuleNameNamespaceContext.class.getName());
    }
}
