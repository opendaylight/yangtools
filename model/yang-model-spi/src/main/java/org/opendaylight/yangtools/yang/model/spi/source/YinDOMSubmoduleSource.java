/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Verify.verifyNotNull;

import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;

/**
 * A {@link YinDOMSource} representing a {@code submodule}.
 */
@NonNullByDefault
public final class YinDOMSubmoduleSource extends YinDOMSource {
    YinDOMSubmoduleSource(final SourceIdentifier sourceId, final DOMSource source, final SourceRefProvider refProvider,
            final @Nullable String symbolicName) {
        super(sourceId, source, refProvider, symbolicName);
    }

    /**
     * {@return the {@code submodule} name}
     */
    public String submoduleName() {
        return verifyNotNull(statement().getLocalName());
    }

    @Override
    public SourceInfo.Submodule extractSourceInfo() throws SourceSyntaxException {
        return new YinDOMSourceInfoExtractor.ForSubmodule(statement(), refProvider()).extractSourceInfo();
    }

    @Override
    public YinDOMSubmoduleSource withSourceId(final SourceIdentifier newSourceId) {
        return newSourceId.equals(sourceId()) ? this
            : new YinDOMSubmoduleSource(newSourceId, domSource(), refProvider(), symbolicName());
    }
}
