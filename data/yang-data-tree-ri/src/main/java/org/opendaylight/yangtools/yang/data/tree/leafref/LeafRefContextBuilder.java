/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

final class LeafRefContextBuilder implements Mutable {
    private final Map<QName, LeafRefContext> referencingChildren = new HashMap<>();
    private final Map<QName, LeafRefContext> referencedByChildren = new HashMap<>();
    private final Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<>();

    private final QName currentNodeQName;
    private final ImmutableList<QName> currentNodePath;
    private final EffectiveModelContext modelContext;

    private LeafRefPath leafRefTargetPath = null;
    private LeafRefPath absoluteLeafRefTargetPath = null;
    private String leafRefTargetPathString = "";

    private boolean isReferencedBy = false;
    private boolean isReferencing = false;

    LeafRefContextBuilder(final QName currentNodeQName, final ImmutableList<QName> currentNodePath,
            final EffectiveModelContext modelContext) {
        this.currentNodeQName = requireNonNull(currentNodeQName);
        this.currentNodePath = requireNonNull(currentNodePath);
        this.modelContext = requireNonNull(modelContext);
    }

    @NonNull LeafRefContext build() {
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
        isReferencedBy = referencedBy;
    }

    boolean isReferencing() {
        return isReferencing;
    }

    void setReferencing(final boolean referencing) {
        isReferencing = referencing;
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

    ImmutableList<QName> getCurrentNodePath() {
        return currentNodePath;
    }

    LeafRefPath getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    void setLeafRefTargetPath(final LeafRefPath leafRefPath) {
        leafRefTargetPath = requireNonNull(leafRefPath);
    }

    String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    void setLeafRefTargetPathString(final String leafRefPathString) {
        leafRefTargetPathString = requireNonNull(leafRefPathString);
    }

    QName getCurrentNodeQName() {
        return currentNodeQName;
    }

    EffectiveModelContext modelContext() {
        return modelContext;
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
        final QNameModule qnameModule = currentNodePath.isEmpty() ? currentNodeQName.getModule()
            : currentNodePath.get(0).getModule();
        return modelContext.findModule(qnameModule).orElse(null);
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
