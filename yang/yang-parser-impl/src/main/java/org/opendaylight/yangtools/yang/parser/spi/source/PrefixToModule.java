/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 *
 * Source-specific mapping of prefixes to namespaces
 *
 */
public interface PrefixToModule extends IdentifierNamespace<String, QNameModule> {

    public static final String DEFAULT_PREFIX = "";


    /**
     *
     * Returns QNameModule (namespace + revision) associated with supplied
     * prefix.
     *
     * @param prefix
     *            Prefix
     * @return QNameModule associated with supplied prefix, or null if prefix is
     *         not defined.
     *
     */
    @Override
    @Nullable QNameModule get(String prefix);

    /**
     *
     * Returns QNameModule (namespace + revision) associated with XML namespace
     * (URI).
     *
     * @param namespace
     *            XML Namespace
     * @return QNameModule associated with supplied namespace, or null if prefix
     *         is not defined.
     *
     */
    @Nullable QNameModule getByNamespace(String namespace);

}
