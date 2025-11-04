/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;

/**
 * A {@link YangIRSource} representing a {@code submodule}.
 */
@NonNullByDefault
public final class YangIRSubmoduleSource extends YangIRSource {
    YangIRSubmoduleSource(final SourceIdentifier sourceId, final IRStatement statement,
            final @Nullable String symbolicName) {
        super(sourceId, statement, symbolicName);
    }

    /**
     * {@return the {@code submodule} name}
     */
    public IRArgument submoduleName() {
        return verifyNotNull(statement().argument());
    }

    @Override
    public SourceInfo.Submodule extractSourceInfo() throws ExtractorException {
        return new YangIRSourceInfoExtractor.ForSubmodule(sourceId(), statement()).extractSourceInfo();
    }
}