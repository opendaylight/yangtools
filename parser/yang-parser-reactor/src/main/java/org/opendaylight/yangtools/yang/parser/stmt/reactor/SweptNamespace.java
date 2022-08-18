/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.AbstractMap;
import java.util.Set;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Placeholder namespace map which does not allow access and acts as a sentinel for namespaces which have been
 * explicitly removed from {@link NamespaceStorageSupport}.
 */
final class SweptNamespace extends AbstractMap<Object, Object> {
    private final ParserNamespace<?, ?> name;

    SweptNamespace(final ParserNamespace<?, ?> name) {
        this.name = requireNonNull(name);
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        throw new VerifyException("Attempted to access swept namespace " + name);
    }
}
