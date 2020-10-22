/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import java.util.Optional;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.expr.VariableReferenceExpr;

abstract class ExprListener {
    void enterBinaryExpr(final BinaryExpr expr) {

    }

    void exitBinaryExpr(final BinaryExpr expr) {

    }

    void enterFilterExpr(final FilterExpr expr) {

    }

    void exitFilterExpr(final FilterExpr expr) {

    }

    void enterFunctionCallExpr(final FunctionCallExpr expr) {

    }

    void exitFunctionCallExpr(final FunctionCallExpr expr) {

    }

    void enterNotExpr(final UnaryExpr expr) {

    }

    void exitNotExpr(final UnaryExpr expr) {

    }

    Optional<StepListener> enterLocationPath(final LocationPath path) {
        return Optional.empty();
    }

    void exitLocationPath(final LocationPath path) {

    }

    void enterPathExpr(final PathExpr expr) {

    }

    void exitPathExpr(final PathExpr expr) {

    }

    void visitLiteralExpr(final LiteralExpr expr) {

    }

    void visitNumberExpr(final NumberExpr expr) {

    }

    void visitOperator(final Operator oper) {

    }

    void visitVariableReferenceExpr(final VariableReferenceExpr expr)  {

    }
}
