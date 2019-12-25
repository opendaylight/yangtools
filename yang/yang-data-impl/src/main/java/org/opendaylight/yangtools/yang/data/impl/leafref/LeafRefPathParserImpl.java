/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.Steps;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangPathExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;

final class LeafRefPathParserImpl {
    private final QNameModule localModule;

    LeafRefPathParserImpl(final SchemaNode currentNode) {
        this.localModule = currentNode.getQName().getModule();
    }

    LeafRefPath parseLeafRefPath(final PathExpression path) {
        final Steps steps = path.getSteps();
        if (steps instanceof LocationPathSteps) {
            return parseLocationPath(((LocationPathSteps) steps).getLocationPath());
        } else if (steps instanceof DerefSteps) {
            throw new UnsupportedOperationException("deref() leafrefs are not implemented yet");
        } else {
            throw new IllegalStateException("Unsupported steps " + steps);
        }
    }

    private LeafRefPath parseLocationPath(final YangLocationPath locationPath) {
        return LeafRefPath.create(createPathSteps(locationPath.getSteps()), locationPath.isAbsolute());
    }

    private Deque<QNameWithPredicate> createPathSteps(final ImmutableList<Step> steps) {
        final Deque<QNameWithPredicate> path = new ArrayDeque<>(steps.size());
        for (Step step : steps) {
            switch (step.getAxis()) {
                case CHILD:
                    checkState(step instanceof QNameStep, "Unsupported step %s", step);
                    path.add(adaptChildStep((QNameStep) step));
                    break;
                case PARENT:
                    path.add(QNameWithPredicate.UP_PARENT);
                    break;
                default:
                    throw new IllegalStateException("Unsupported axis in step " + step);
            }
        }
        return path;
    }

    private QNameWithPredicate adaptChildStep(final QNameStep step) {
        final QName qname = resolve(step.getQName());
        final Set<YangExpr> predicates = step.getPredicates();
        if (predicates.isEmpty()) {
            return new SimpleQNameWithPredicate(qname);
        }

        final QNameWithPredicateBuilder builder = new QNameWithPredicateBuilder(qname.getModule(),
            qname.getLocalName());

        for (YangExpr pred : predicates) {
            final QNamePredicateBuilder predBuilder = new QNamePredicateBuilder();

            if (pred instanceof YangBinaryExpr) {
                final YangBinaryExpr eqPred = (YangBinaryExpr) pred;
                checkState(eqPred.getOperator() == YangBinaryOperator.EQUALS);

                final YangExpr left = eqPred.getLeftExpr();
                checkState(left instanceof YangQNameExpr, "Unsupported left expression %s", left);
                predBuilder.setIdentifier(resolve(((YangQNameExpr) left).getQName()));

                final YangExpr right = eqPred.getRightExpr();
                if (right instanceof YangPathExpr) {
                    final YangPathExpr rightPath = (YangPathExpr) right;
                    final YangExpr filter = rightPath.getFilterExpr();
                    if (filter instanceof YangFunctionCallExpr) {
                        checkState(YangFunction.CURRENT.getIdentifier().equals(
                            ((YangFunctionCallExpr) filter).getName()));
                    } else {
                        throw new IllegalStateException("Unhandled filter " + filter);
                    }

                    final Relative location = rightPath.getLocationPath()
                            .orElseThrow(() -> new IllegalStateException("Missing locationPath in " + rightPath));
                    predBuilder.setPathKeyExpression(LeafRefPath.create(createPathSteps(location.getSteps()), false));
                } else {
                    throw new UnsupportedOperationException("Not implemented for " + right);
                }
            }

            builder.addQNamePredicate(predBuilder.build());
        }

        return builder.build();
    }

    private QName resolve(final AbstractQName qname) {
        if (qname instanceof QName) {
            return (QName) qname;
        }
        if (qname instanceof UnqualifiedQName) {
            // Bind to namespace. Note we expect to perform frequent matching, hence we are interning the result
            return ((UnqualifiedQName) qname).bindTo(localModule).intern();
        }
        throw new IllegalStateException("Unhandled unresolved QName " + qname);
    }
}
