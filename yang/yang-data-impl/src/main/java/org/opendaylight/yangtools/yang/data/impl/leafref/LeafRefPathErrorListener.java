/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LeafRefPathErrorListener extends BaseErrorListener {
    private static final Logger LOG = LoggerFactory.getLogger(LeafRefPathErrorListener.class);

    private final List<LeafRefPathSyntaxErrorException> exceptions = new ArrayList<>(1);
    private final Module module;

    LeafRefPathErrorListener(final Module module) {
        this.module = module;
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException e) {
        LOG.debug("Syntax error in module {} at {}:{}: {}", module.getName(), line, charPositionInLine, msg, e);

        exceptions.add(new LeafRefPathSyntaxErrorException(module.getName(), line, charPositionInLine, msg, e));
    }

    public void validate() throws LeafRefPathSyntaxErrorException {
        if (exceptions.isEmpty()) {
            return;
        }

        // Single exception: just throw it
        if (exceptions.size() == 1) {
            throw exceptions.get(0);
        }

        final StringBuilder sb = new StringBuilder();
        String module = null;
        boolean first = true;
        for (final LeafRefPathSyntaxErrorException e : exceptions) {
            if (module == null) {
                module = e.getModule();
            }
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }

            sb.append(e.getFormattedMessage());
        }

        throw new LeafRefPathSyntaxErrorException(module, 0, 0, sb.toString());
    }

}
