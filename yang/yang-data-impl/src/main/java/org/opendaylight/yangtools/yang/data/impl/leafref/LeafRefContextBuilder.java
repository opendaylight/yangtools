/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class LeafRefContextBuilder implements Builder<LeafRefContext> {

    private final Map<QName, LeafRefContext> referencingChildren = new HashMap<>();
    private final Map<QName, LeafRefContext> referencedByChildren = new HashMap<>();
    private final Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<>();

    private final QName currentNodeQName;
    private final SchemaPath currentNodePath;
    private final SchemaContext schemaContext;

    private PathExpression leafRefTargetPath = null;
    private LeafRefPath absoluteLeafRefTargetPath = null;
    private final String leafRefTargetPathString = "";

    private boolean isReferencedBy = false;
    private boolean isReferencing = false;

    LeafRefContextBuilder(final QName currentNodeQName, final SchemaPath currentNodePath,
        final SchemaContext schemaContext) {
        this.currentNodeQName = requireNonNull(currentNodeQName);
        this.currentNodePath = requireNonNull(currentNodePath);
        // FIXME: requireNonNull
        this.schemaContext = schemaContext;
    }

    @Override
    public LeafRefContext build() {
        final LeafRefContext leafRefContext = new LeafRefContext(this);

        // LeafRefContext has made a copy of these
        referencingChildren.clear();
        referencedByChildren.clear();
        referencedByLeafRefCtx.clear();

        return leafRefContext;
    }

    boolean isReferencedBy() {
        return isReferencedBy;
    }

    void setReferencedBy(final boolean referencedBy) {
        this.isReferencedBy = referencedBy;
    }

    boolean isReferencing() {
        return isReferencing;
    }

    void setReferencing(final boolean referencing) {
        this.isReferencing = referencing;
    }

    void addReferencingChild(final LeafRefContext child, final QName childQName) {
        referencingChildren.put(childQName, child);
    }

    Map<QName, LeafRefContext> getReferencingChilds() {
        return referencingChildren;
    }

    void addReferencedByChild(final LeafRefContext child, final QName childQName) {
        referencedByChildren.put(childQName, child);
    }

    Map<QName, LeafRefContext> getReferencedByChilds() {
        return referencedByChildren;
    }

    SchemaPath getCurrentNodePath() {
        return currentNodePath;
    }

    PathExpression getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    void setLeafRefTargetPath(final PathExpression leafRefPath) {
        this.leafRefTargetPath = leafRefPath;
    }

    String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    QName getCurrentNodeQName() {
        return currentNodeQName;
    }

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    LeafRefPath getAbsoluteLeafRefTargetPath() {
        if (isReferencing && absoluteLeafRefTargetPath == null) {
            if (leafRefTargetPath.isAbsolute()) {
                absoluteLeafRefTargetPath = leafRefTargetPath;
            } else {
                absoluteLeafRefTargetPath = LeafRefUtils.createAbsoluteLeafRefPath(leafRefTargetPath,
                    currentNodePath, getLeafRefContextModule());
            }
        }

        return absoluteLeafRefTargetPath;
    }

    Module getLeafRefContextModule() {
        final Iterator<QName> it = currentNodePath.getPathFromRoot().iterator();
        final QNameModule qnameModule = it.hasNext() ? it.next().getModule() : currentNodeQName.getModule();
        return schemaContext.findModule(qnameModule).orElse(null);
    }

    void addReferencedByLeafRefCtx(final QName qname, final LeafRefContext leafRef) {
        referencedByLeafRefCtx.put(qname, leafRef);
    }

    LeafRefContext getReferencedByLeafRefCtxByName(final QName qname) {
        return referencedByLeafRefCtx.get(qname);
    }

    Map<QName, LeafRefContext> getAllReferencedByLeafRefCtxs() {
        return referencedByLeafRefCtx;
    }
}
