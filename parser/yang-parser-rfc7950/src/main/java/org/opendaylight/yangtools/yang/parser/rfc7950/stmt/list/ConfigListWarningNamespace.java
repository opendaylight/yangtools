/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

@Beta
public final class ConfigListWarningNamespace {
    public static final @NonNull ParserNamespace<StatementSourceReference, Boolean> INSTANCE =
        new ParserNamespace<>("configListWarning");
    public static final @NonNull NamespaceBehaviour<?, ?> BEHAVIOUR = NamespaceBehaviour.global(INSTANCE);

    private ConfigListWarningNamespace() {
        // Hidden on purpose
    }
}
