/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.net.URISyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * Source-specific mapping of prefixes to namespaces.
 */
public abstract class PrefixToModule extends AbstractParserNamespace<String, QNameModule> {
    // FIXME: document this constant
    public final @NonNull String DEFAULT_PREFIX = "";

    protected PrefixToModule() {
        super(ModelProcessingPhase.SOURCE_LINKAGE, NamespaceBehaviour.global(PrefixToModule.class));
    }

    /**
     * Returns QNameModule (namespace + revision) associated with supplied prefix.
     *
     * @param prefix Prefix
     * @return QNameModule associated with supplied prefix, or null if prefix is not defined.
     */
    public abstract @Nullable QNameModule get(String prefix);

    /**
     * Returns QNameModule (namespace + revision) associated with XML namespace (URI).
     *
     * @param namespace XML Namespace
     * @return QNameModule associated with supplied namespace, or null if prefix is not defined.
     * @throws URISyntaxException if the input string is not valid URI
     */
    public abstract @Nullable QNameModule getByNamespace(String namespace) throws URISyntaxException;
}
