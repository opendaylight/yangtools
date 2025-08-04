/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;

public class ExtendedSourceInfo {
    private final SourceInfo sourceInfo;
    //TODO: try getting rid of this YangIRSource and use the SourceInfo directly - thus remove this class altogether
    private final YangIRSource source;

    public ExtendedSourceInfo(SourceInfo sourceInfo, YangIRSource source) {
        this.sourceInfo = sourceInfo;
        this.source = source;
    }

    public SourceInfo getSourceInfo() {
        return sourceInfo;
    }

    public YangIRSource getIRSource() {
        return source;
    }
}
