/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * Implementation-internal cache for looking up URI to import prefix. URIs are taken in as Strings to save ourselves
 * some quality parsing time.
 */
@Beta
public interface URIStringToImportPrefix extends IdentifierNamespace<String, String> {
    NamespaceBehaviour<String, String, @NonNull URIStringToImportPrefix> BEHAVIOUR =
            NamespaceBehaviour.sourceLocal(URIStringToImportPrefix.class);
}
