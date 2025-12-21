/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser.SyntaxOnly;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

@NonNullByDefault
final class IdentifierParser extends AbstractArgumentParser<QName> implements SyntaxOnly<QName> {
    final NamespaceBinding namespaceBinding;

    /**
     * Construct an instance using specified {@link NamespaceBinding}.
     *
     * @param namespaceBinding the {@link NamespaceBinding}
     */
    IdentifierParser(final NamespaceBinding namespaceBinding) {
        this.namespaceBinding = requireNonNull(namespaceBinding);
    }

    @Override
    public QName parseArgument(final String rawArgument) throws ArgumentSyntaxException {
        return parseIdentifier(namespaceBinding.currentModule(), rawArgument, 0);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("currentModule", namespaceBinding.currentModule());
    }

    QName parseIdentifier(final String rawArgument, final int beginIndex) throws ArgumentSyntaxException {
        return parseIdentifier(namespaceBinding.currentModule(), rawArgument, beginIndex);
    }

    static QName parseIdentifier(final QNameModule module, final String rawArgument, final int beginIndex)
            throws ArgumentSyntaxException {
        final var identifier = UnresolvedQName.tryLocalName(rawArgument);
        if (identifier == null) {
            throw new ArgumentSyntaxException("'" + rawArgument + "' is not a valid identifier", beginIndex + 1);
        }
        return identifier.bindTo(module).intern();
    }
}
