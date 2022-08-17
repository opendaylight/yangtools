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
 * Namespace for remembering the {@code yang-data} argument's QName.
 */
@Beta
// FIXME: We should not be needing this namespace, as yang-data's argument is not documented anywhere to be compatible
//        with 'identifier', hence we cannot safely form a QName.
public final class YangDataArgumentNamespace extends ParserNamespace<Empty, QName> {
    public static final @NonNull YangDataArgumentNamespace INSTANCE = new YangDataArgumentNamespace();
    public static final @NonNull NamespaceBehaviour<?, ? ,?> BEHAVIOUR =
        NamespaceBehaviour.statementLocal(YangDataArgumentNamespace.class);

    private YangDataArgumentNamespace() {
        // Hidden on purpose
    }
}
