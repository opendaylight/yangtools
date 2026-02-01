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
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRToTextSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * An ANTLR-based {@link YangTextToIRSourceTransformer}.
 */
@Component(service = { SourceTransformer.class, YangIRToTextSourceTransformer.class })
@MetaInfServices(value = { SourceTransformer.class, YangIRToTextSourceTransformer.class })
@NonNullByDefault
public final class DefaultYangIRToTextSourceTransformer implements YangIRToTextSourceTransformer {
    @Activate
    public DefaultYangIRToTextSourceTransformer() {
        // Nothing else
    }

    @Override
    public YangTextSource transformSource(final YangIRSource source) {
        return new StringYangTextSource(source.sourceId(), source.statement().prettyTree().toString());
    }
}
