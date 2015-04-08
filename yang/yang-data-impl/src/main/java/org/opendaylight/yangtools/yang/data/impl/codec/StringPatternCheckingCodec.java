/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StringPatternCheckingCodec extends StringStringCodec {

    private static final Logger LOG = LoggerFactory.getLogger(StringPatternCheckingCodec.class);
    private final List<CompiledPatternContext> patterns;

    protected StringPatternCheckingCodec(final StringTypeDefinition typeDef) {
        super(typeDef);
        patterns = new ArrayList<>(typeDef.getPatternConstraints().size());
        for (final PatternConstraint yangPattern : typeDef.getPatternConstraints()) {
            try {
                patterns.add(new CompiledPatternContext(yangPattern));
            } catch (final PatternSyntaxException e) {
                LOG.debug("Unable to compile {} pattern, excluding it from validation.", yangPattern, e);
            }
        }
    }

    @Override
    protected void validate(final String s) {
        super.validate(s);
        for (final CompiledPatternContext pattern : patterns) {
            pattern.validate(s);
        }
    }

}