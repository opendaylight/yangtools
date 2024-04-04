/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StringPatternCheckingCodec extends StringStringCodec {
    private static final Logger LOG = LoggerFactory.getLogger(StringPatternCheckingCodec.class);

    private final ImmutableList<CompiledPatternContext> patterns;

    StringPatternCheckingCodec(final StringTypeDefinition typeDef) {
        super(typeDef);

        final List<PatternConstraint> constraints = typeDef.getPatternConstraints();
        final Builder<CompiledPatternContext> builder = ImmutableList.builderWithExpectedSize(constraints.size());
        for (final PatternConstraint yangPattern : typeDef.getPatternConstraints()) {
            try {
                builder.add(new CompiledPatternContext(yangPattern));
            } catch (final PatternSyntaxException e) {
                LOG.debug("Unable to compile {} pattern, excluding it from validation.", yangPattern, e);
            }
        }
        patterns = builder.build();
    }

    @Override
    void validate(final String str) {
        super.validate(str);
        for (final CompiledPatternContext pattern : patterns) {
            pattern.validate(str);
        }
    }

}