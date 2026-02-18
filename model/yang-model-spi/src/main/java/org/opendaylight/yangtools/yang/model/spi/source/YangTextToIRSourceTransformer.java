/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link SourceTransformer} capable of turning {@link YangTextSource} into a {@link YangIRSource}.
 */
@NonNullByDefault
public interface YangTextToIRSourceTransformer extends SourceTransformer<YangTextSource, YangIRSource> {
    @Override
    default Class<YangTextSource> inputRepresentation() {
        return YangTextSource.class;
    }

    @Override
    default Class<YangIRSource> outputRepresentation() {
        return YangIRSource.class;
    }
}
