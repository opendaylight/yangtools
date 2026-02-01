/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.source.ir;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * An ANTLR-based {@link YangTextToIRSourceTransformer}.
 */
@Component(service = { SourceTransformer.class, YangTextToIRSourceTransformer.class })
@MetaInfServices(value = { SourceTransformer.class, YangTextToIRSourceTransformer.class })
@NonNullByDefault
public final class DefaultYangTextToIRSourceTransformer implements YangTextToIRSourceTransformer {
    @Activate
    public DefaultYangTextToIRSourceTransformer() {
        // Nothing else
    }

    @Override
    public YangIRSource transformSource(final YangTextSource input) throws SourceSyntaxException {
        return YangIRSource.of(input.sourceId(), IRSupport.createStatement(YangTextParser.parseSource(input)),
            input.symbolicName());
    }
}
