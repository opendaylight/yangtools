/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

/**
 * A namespace declaration attribute. This is internal-only and not directly exposed to users, at least for now
 */
sealed interface NamespaceDeclaration extends Attribute
        permits DefaultNamespaceDeclaration, PrefixNamespaceDeclaration {
    @Override
    default boolean isNamespace() {
        return true;
    }
}
