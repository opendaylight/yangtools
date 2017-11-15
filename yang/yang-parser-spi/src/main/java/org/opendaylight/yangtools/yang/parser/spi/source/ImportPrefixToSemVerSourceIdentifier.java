/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * Source-specific mapping of prefixes to source identifier with specified semantic version.
 */
@Beta
public interface ImportPrefixToSemVerSourceIdentifier
    extends IdentifierNamespace<String, SemVerSourceIdentifier> {
    NamespaceBehaviour<String, SemVerSourceIdentifier, @NonNull ImportPrefixToSemVerSourceIdentifier> BEHAVIOUR =
        NamespaceBehaviour.sourceLocal(ImportPrefixToSemVerSourceIdentifier.class);

}
