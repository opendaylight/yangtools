/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import com.google.common.annotations.Beta;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Statement-local namespace carrying the versioning information about how the import was satisfied.
 */
@Beta
public final class ImportedVersionNamespace extends ParserNamespace<Empty, SourceIdentifier> {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR =
        NamespaceBehaviour.statementLocal(ImportedVersionNamespace.class);

    private ImportedVersionNamespace() {
        // Hidden on purpose
    }
}
