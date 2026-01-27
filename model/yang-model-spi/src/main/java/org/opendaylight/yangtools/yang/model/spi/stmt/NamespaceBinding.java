/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;

/**
 * Interface for binding {@code prefix}-based {code identifier} ABNF constructs -- for example as used in
 * {@code identifier-ref}, {@code unknown-statement} and {@code node-identifier} ABNF productions.
 *
 * @since 14.0.22
 */
// FIXME: 15.0.0: This looks awfully like yang.common.YangNamespaceContext, except it is better and more encompassing.
//                Try unify the two somehow, keeping in mind that YangXPathParserFactory is using both of these.
@Beta
@NonNullByDefault
public interface NamespaceBinding {
    /**
     * {@return the current module, i.e. the {@link QNameModule} a plain {@code identifier}s are bound to}
     */
    QNameModule currentModule();

    /**
     * {@return the module for specified prefix, or {@code null} if no such module exists}
     * @param prefix the prefix
     */
    @Nullable QNameModule lookupModule(UnresolvedQName.Unqualified prefix);

    default QName resolveQName(final UnresolvedQName unresolved) throws ArgumentBindingException {
        return switch (unresolved) {
            case UnresolvedQName.Qualified qualified -> resolveQName(qualified);
            case UnresolvedQName.Unqualified unqualified -> resolveQName(unqualified);
        };
    }

    default QName resolveQName(final UnresolvedQName.Qualified qualified) throws ArgumentBindingException {
        final var prefix = qualified.getPrefix();
        // FIXME: can we side-step checking here?
        final var module = lookupModule(UnresolvedQName.Unqualified.of(prefix));
        if (module == null) {
            throw new ArgumentBindingException("Prefix '" + prefix + "' cannot be resolved", 0);
        }
        return qualified.bindTo(module);
    }

    default QName resolveQName(final UnresolvedQName.Unqualified unqualified) {
        return unqualified.bindTo(currentModule());
    }
}