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
 * A {@link YangIRSource} representing a {@code module}.
 */
@NonNullByDefault
public final class YangIRModuleSource extends YangIRSource {
    YangIRModuleSource(final SourceIdentifier sourceId, final IRStatement statement,
            final @Nullable String symbolicName) {
        super(sourceId, statement, symbolicName);
    }

    /**
     * {@return the {@code module} name}
     */
    public IRArgument moduleName() {
        return verifyNotNull(statement().argument());
    }

    @Override
    public SourceInfo.Module extractSourceInfo() throws ExtractorException {
        return new YangIRSourceInfoExtractor.ForModule(sourceId(), statement()).extractSourceInfo();
    }
}