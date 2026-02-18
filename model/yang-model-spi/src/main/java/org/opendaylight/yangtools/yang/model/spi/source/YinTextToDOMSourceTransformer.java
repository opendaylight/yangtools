/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;

/**
 * A {@link SourceTransformer} capable of turning {@link YinTextSource} into a {@link YinDOMSource}.
 */
@NonNullByDefault
public interface YinTextToDOMSourceTransformer extends SourceTransformer<YinTextSource, YinDOMSource> {
    @Override
    default Class<YinTextSource> inputRepresentation() {
        return YinTextSource.class;
    }

    @Override
    default Class<YinDOMSource> outputRepresentation() {
        return YinDOMSource.class;
    }
}
