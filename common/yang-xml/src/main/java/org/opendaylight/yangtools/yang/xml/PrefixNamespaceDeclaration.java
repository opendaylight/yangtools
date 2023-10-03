/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import javax.xml.XMLConstants;
import org.eclipse.jdt.annotation.NonNull;

record PrefixNamespaceDeclaration(@NonNull String prefix, @NonNull String value) implements NamespaceDeclaration {
    PrefixNamespaceDeclaration {
        requireNonNull(prefix);
        requireNonNull(value);
    }

    @Override
    public String localName() {
        return XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix;
    }

    @Override
    public String namespace() {
        return XMLNS_ATTRIBUTE_NS_URI;
    }

    @Override
    public boolean isDefaultNamespace() {
        return false;
    }

    @Override
    public boolean isNamespacePrefix() {
        return true;
    }
}
