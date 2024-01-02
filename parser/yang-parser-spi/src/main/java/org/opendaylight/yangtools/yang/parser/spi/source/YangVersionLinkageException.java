/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;

/**
 * An exception indicating source-level problem across multiple YANG sources relating to how they are allowed to be
 * linked between YANG versions. This typically indicates a direct violation of
 * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-12">RFC7950 section 12</a>.
 */
@Beta
public class YangVersionLinkageException extends SourceException {
    private static final long serialVersionUID = 1L;

    public YangVersionLinkageException(final @NonNull StatementSourceReference source, final @NonNull String format,
            final Object... args) {
        super(source, format, args);
    }

    public YangVersionLinkageException(final @NonNull CommonStmtCtx stmt, final @NonNull String format,
            final Object... args) {
        this(stmt.sourceReference(), format, args);
    }
}
