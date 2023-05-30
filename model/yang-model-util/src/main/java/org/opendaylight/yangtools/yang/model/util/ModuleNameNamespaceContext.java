/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableBiMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.spi.AbstractEffectiveModelContextProvider;

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
public final class ModuleNameNamespaceContext extends AbstractEffectiveModelContextProvider
        implements YangNamespaceContext {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Handled through writeReplace()")
    public ModuleNameNamespaceContext(final EffectiveModelContext schemaContext) {
        super(schemaContext);
    }

    /**
     * Convert this object to an equivalent {@link BiMapYangNamespaceContext}.
     *
     * @return A BiMapYangNamespaceContext.
     */
    public BiMapYangNamespaceContext toBiMap() {
        final var builder = ImmutableBiMap.<String, QNameModule>builder();
        for (var module : getEffectiveModelContext().getModuleStatements().values()) {
            final var name = module.argument().getLocalName();
            builder.put(name, findNamespaceForPrefix(name).orElseThrow());
        }
        return new BiMapYangNamespaceContext(builder.build());
    }

    @Override
    public QNameModule namespaceForPrefix(final String prefix) {
        return getEffectiveModelContext().findModules(prefix).stream().findFirst().map(Module::getQNameModule)
            .orElse(null);
    }

    @Override
    public String prefixForNamespace(final QNameModule namespace) {
        return getEffectiveModelContext().findModule(namespace).map(Module::getName).orElse(null);
    }

    @java.io.Serial
    private Object writeReplace() {
        return toBiMap();
    }
}
