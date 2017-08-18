/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.jaxen.expr.AllNodeStep;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.CommentNodeStep;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.ProcessingInstructionNodeStep;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.expr.VariableReferenceExpr;

final class ExprWalker {
    private final ExprListener listener;

    ExprWalker(final ExprListener listener) {
        this.listener = requireNonNull(listener);
    }

    public void walk(final Expr expr) {
        if (expr instanceof BinaryExpr) {
            final BinaryExpr binary = (BinaryExpr) expr;
            listener.enterBinaryExpr(binary);
            walk(binary.getLHS());
            listener.visitOperator(Operator.forString(binary.getOperator()));
            walk(binary.getRHS());
            listener.exitBinaryExpr(binary);
        } else if (expr instanceof FilterExpr) {
            final FilterExpr filter = (FilterExpr) expr;
            listener.enterFilterExpr(filter);
            walk(expr);
            listener.exitFilterExpr(filter);
        } else if (expr instanceof FunctionCallExpr) {
            final FunctionCallExpr func = (FunctionCallExpr) expr;
            listener.enterFunctionCallExpr(func);

            for (Object arg : func.getParameters()) {
                walk((Expr) arg);
            }

            listener.exitFunctionCallExpr(func);
        } else if (expr instanceof LiteralExpr) {
            listener.visitLiteralExpr((LiteralExpr) expr);
        } else if (expr instanceof LocationPath) {
            final LocationPath path = (LocationPath) expr;
            final Optional<StepListener> maybeListener = listener.enterLocationPath(path);
            if (maybeListener.isPresent()) {
                final StepListener l = maybeListener.get();
                for (Object step : path.getSteps()) {
                    if (step instanceof AllNodeStep) {
                        l.onAll((AllNodeStep) step);
                    } else if (step instanceof CommentNodeStep) {
                        l.onComment((CommentNodeStep) step);
                    } else if (step instanceof NameStep) {
                        l.onName((NameStep) step);
                    } else if (step instanceof ProcessingInstructionNodeStep) {
                        l.onProcessingInstruction((ProcessingInstructionNodeStep) step);
                    } else if (step instanceof TextNodeStep) {
                        l.onTest((TextNodeStep) step);
                    } else {
                        throw new IllegalArgumentException("Unsupported step " + step);
                    }
                }
            }

            listener.exitLocationPath(path);
        } else if (expr instanceof NumberExpr) {
            listener.visitNumberExpr((NumberExpr) expr);
        } else if (expr instanceof PathExpr) {
            final PathExpr path = (PathExpr) expr;
            listener.enterPathExpr(path);
            walk(path.getFilterExpr());
            walk(path.getLocationPath());
            listener.exitPathExpr(path);
        } else if (expr instanceof UnaryExpr) {
            final UnaryExpr unary = (UnaryExpr) expr;
            listener.enterNotExpr(unary);
            walk(unary.getExpr());
            listener.exitNotExpr(unary);
        } else if (expr instanceof VariableReferenceExpr) {
            listener.visitVariableReferenceExpr((VariableReferenceExpr) expr);
        } else {
            throw new IllegalArgumentException("Unsupported expression " + expr);
        }
    }
}
