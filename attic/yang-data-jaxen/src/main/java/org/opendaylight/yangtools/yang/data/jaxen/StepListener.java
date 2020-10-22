/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import org.jaxen.expr.AllNodeStep;
import org.jaxen.expr.CommentNodeStep;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.ProcessingInstructionNodeStep;
import org.jaxen.expr.TextNodeStep;

abstract class StepListener {

    void onAll(final AllNodeStep step) {

    }

    void onComment(final CommentNodeStep step) {

    }

    void onName(final NameStep step) {

    }

    void onProcessingInstruction(final ProcessingInstructionNodeStep step) {

    }

    void onTest(final TextNodeStep step) {

    }
}
