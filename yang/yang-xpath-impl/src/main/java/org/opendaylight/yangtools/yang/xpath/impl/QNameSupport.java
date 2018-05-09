/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

final class QNameSupport {
    private final Function<String, QNameModule> prefixes;
    private final QNameModule implicitNamespace;

    QNameSupport(final QNameModule implicitNamespace, final Function<String, QNameModule> prefixes) {
        this.implicitNamespace = requireNonNull(implicitNamespace);
        this.prefixes = requireNonNull(prefixes);
    }

    QName createQName(final String localName) {
        return QName.create(implicitNamespace, localName);
    }

    QName createQName(final String prefix, final String localName) {
        final QNameModule namespace = prefixes.apply(prefix);
        checkArgument(namespace != null, "Failed to lookup namespace for prefix %s", prefix);
        return QName.create(namespace, localName);
    }

    Optional<QNameModule> resolvePrefix(final String prefix) {
        return Optional.ofNullable(prefixes.apply(requireNonNull(prefix)));
    }
}
