/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Namespace for remembering the {@code yang-data} argument's QName. This namespace is necessary because we are forced
 * to have a String argument due to us being an extension.
 */
@Beta
// FIXME: YANGTOOLS-1196: remove this namespace once we can bind freely
public interface YangDataArgumentNamespace extends ParserNamespace<Empty, QName> {
    NamespaceBehaviour<Empty, QName, @NonNull YangDataArgumentNamespace> BEHAVIOUR =
        NamespaceBehaviour.statementLocal(YangDataArgumentNamespace.class);

}
