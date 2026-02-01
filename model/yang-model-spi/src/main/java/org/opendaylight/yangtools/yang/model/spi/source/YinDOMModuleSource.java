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
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;

/**
 * A {@link YinDomSource} representing a {@code module}.
 */
@NonNullByDefault
public final class YinDOMModuleSource extends YinDomSource {
    YinDOMModuleSource(final SourceIdentifier sourceId, final DOMSource source, final SourceRefProvider refProvider,
            final @Nullable String symbolicName) {
        super(sourceId, source, refProvider, symbolicName);
    }

    /**
     * {@return the {@code module} name}
     */
    public String moduleName() {
        return verifyNotNull(statement().getLocalName());
    }

    @Override
    public SourceInfo.Module extractSourceInfo() throws ExtractorException {
        return new YinDomSourceInfoExtractor.ForModule(statement(), refProvider()).extractSourceInfo();
    }

    @Override
    public YinDOMModuleSource withSourceId(final SourceIdentifier newSourceId) {
        return newSourceId.equals(sourceId()) ? this
            : new YinDOMModuleSource(newSourceId, domSource(), refProvider(), symbolicName());
    }
}
