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
import org.opendaylight.yangtools.yang.common.QualifiedQName;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.Steps;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;

final class LeafRefPathParserImpl {
    private final SchemaContext schemaContext;
    private final Module module;
    private final SchemaNode node;

    LeafRefPathParserImpl(final SchemaContext schemaContext, final Module currentModule, final SchemaNode currentNode) {
        this.schemaContext = schemaContext;
        this.module = currentModule;
        this.node = currentNode;
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
        final ImmutableList<Step> steps = locationPath.getSteps();
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

        return LeafRefPath.create(path, locationPath.isAbsolute());
    }

    private QNameWithPredicate adaptChildStep(final QNameStep step) {
        final AbstractQName unresolved = step.getQName();
        final QName qname;
        if (unresolved instanceof QName) {
            qname = (QName) unresolved;
        } else if (unresolved instanceof UnqualifiedQName) {
            // Bind to namespace. Note we expect to perform frequent matching, hence we are interning the result
            qname = ((UnqualifiedQName) unresolved).bindTo(node.getQName().getModule()).intern();
        } else if (unresolved instanceof QualifiedQName) {
            throw new UnsupportedOperationException("QName resolution not implemented for " + unresolved);
        } else {
            throw new IllegalStateException("Unhandled unresolved QName " + unresolved);
        }

        final Set<YangExpr> predicates = step.getPredicates();
        if (predicates.isEmpty()) {
            return new SimpleQNameWithPredicate(qname);
        }

        throw new UnsupportedOperationException("Predicate translation not implemented for " + predicates);
    }
}
