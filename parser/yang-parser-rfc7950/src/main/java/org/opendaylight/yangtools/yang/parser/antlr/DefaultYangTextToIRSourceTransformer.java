/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoExtractors;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * An ANTLR-based {@link YangTextToIRSourceTransformer}.
 */
@Component
@MetaInfServices
@NonNullByDefault
public final class DefaultYangTextToIRSourceTransformer implements YangTextToIRSourceTransformer {
    @Activate
    public DefaultYangTextToIRSourceTransformer() {
        // Nothing else
    }

    @Override
    public YangIRSource transformSource(final YangTextSource input) throws ExtractorException, SourceSyntaxException {
        final var rootStatement = IRSupport.createStatement(YangTextParser.parseSource(input));
        final var info = SourceInfoExtractors.forIR(rootStatement, input.sourceId()).extractSourceInfo();
        return YangIRSource.of(info.sourceId(), rootStatement, input.symbolicName());
    }
}
