/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

public final class LeafRefContext {
    private final @NonNull EffectiveModelContext modelContext;

    private final QName currentNodeQName;
    private final ImmutableList<QName> currentNodePath;
    private final Module module;

    private final LeafRefPath leafRefTargetPath;
    private final LeafRefPath absoluteLeafRefTargetPath;
    private final String leafRefTargetPathString;

    private final boolean isReferencedBy;
    private final boolean isReferencing;

    private final ImmutableMap<QName, LeafRefContext> referencingChilds;
    private final ImmutableMap<QName, LeafRefContext> referencedByChilds;
    private final ImmutableMap<QName, LeafRefContext> referencedByLeafRefCtx;

    // FIXME: this looks like it's related to absoluteLeafRefTargetPath, but the original use in LeafRefValidation
    //        fast path did not make it clear. Analyze the relationship between this field and
    //        absoluteLeafRefTargetPath.
    private volatile LeafRefPath leafRefNodePath = null;

    LeafRefContext(final LeafRefContextBuilder leafRefContextBuilder) {
        modelContext = requireNonNull(leafRefContextBuilder.modelContext());
        currentNodeQName = leafRefContextBuilder.getCurrentNodeQName();
        currentNodePath = leafRefContextBuilder.getCurrentNodePath();
        leafRefTargetPath = leafRefContextBuilder.getLeafRefTargetPath();
        absoluteLeafRefTargetPath = leafRefContextBuilder.getAbsoluteLeafRefTargetPath();
        leafRefTargetPathString = leafRefContextBuilder.getLeafRefTargetPathString();
        isReferencedBy = leafRefContextBuilder.isReferencedBy();
        isReferencing = leafRefContextBuilder.isReferencing();
        referencingChilds = ImmutableMap.copyOf(leafRefContextBuilder.getReferencingChilds());
        referencedByChilds = ImmutableMap.copyOf(leafRefContextBuilder.getReferencedByChilds());
        referencedByLeafRefCtx = ImmutableMap.copyOf(leafRefContextBuilder.getAllReferencedByLeafRefCtxs());
        module = leafRefContextBuilder.getLeafRefContextModule();
    }

    public @NonNull EffectiveModelContext modelContext() {
        return modelContext;
    }


    public static LeafRefContext create(final EffectiveModelContext ctx) {
        return new LeafRefContextTreeBuilder(ctx).buildLeafRefContextTree();
    }

    public boolean hasLeafRefContextChild() {
        return hasReferencedChild() || hasReferencingChild();
    }

    public boolean hasReferencedChild() {
        return !referencedByChilds.isEmpty();
    }

    public boolean hasReferencingChild() {
        return !referencingChilds.isEmpty();
    }

    public boolean isReferenced() {
        return isReferencedBy;
    }

    public boolean isReferencing() {
        return isReferencing;
    }

    public LeafRefContext getReferencingChildByName(final QName name) {
        return referencingChilds.get(name);
    }

    public Map<QName, LeafRefContext> getReferencingChilds() {
        return referencingChilds;
    }

    public LeafRefContext getReferencedChildByName(final QName name) {
        return referencedByChilds.get(name);
    }

    public Map<QName, LeafRefContext> getReferencedByChilds() {
        return referencedByChilds;
    }

    public ImmutableList<QName> getCurrentNodePath() {
        return currentNodePath;
    }

    public LeafRefPath getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    public String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    public QName getNodeName() {
        return currentNodeQName;
    }

    public LeafRefPath getAbsoluteLeafRefTargetPath() {
        return absoluteLeafRefTargetPath;
    }

    public Module getLeafRefContextModule() {
        return module;
    }

    public LeafRefContext getReferencedByLeafRefCtxByName(final QName qname) {
        return referencedByLeafRefCtx.get(qname);
    }

    public Map<QName, LeafRefContext> getAllReferencedByLeafRefCtxs() {
        return referencedByLeafRefCtx;
    }

    @Beta
    public LeafRefContext getLeafRefReferencingContext(final SchemaNodeIdentifier node) {
        final Iterator<QName> iterator = descendantIterator(node);
        LeafRefContext leafRefCtx = null;
        LeafRefContext current = this;
        while (iterator.hasNext() && current != null) {
            final QName qname = iterator.next();
            leafRefCtx = current.getReferencingChildByName(qname);
            if (iterator.hasNext()) {
                current = leafRefCtx;
            }
        }

        return leafRefCtx;
    }

    @Beta
    public LeafRefContext getLeafRefReferencedByContext(final SchemaNodeIdentifier node) {
        final Iterator<QName> iterator = descendantIterator(node);
        LeafRefContext leafRefCtx = null;
        LeafRefContext current = this;
        while (iterator.hasNext() && current != null) {
            final QName qname = iterator.next();
            leafRefCtx = current.getReferencedChildByName(qname);
            if (iterator.hasNext()) {
                current = leafRefCtx;
            }
        }

        return leafRefCtx;
    }

    private Iterator<QName> descendantIterator(final SchemaNodeIdentifier node) {
        final Iterator<QName> nodeSteps = node.getNodeIdentifiers().iterator();
        if (node instanceof SchemaNodeIdentifier.Absolute) {
            for (QName myNext : currentNodePath) {
                checkArgument(nodeSteps.hasNext(), "Node %s is an ancestor of %s", node, currentNodePath);
                final QName nodeNext = nodeSteps.next();
                checkArgument(myNext.equals(nodeNext), "Node %s is not a descendant of %s", node, currentNodePath);
            }
        }
        return nodeSteps;
    }

    @Beta
    public boolean isLeafRef(final SchemaNodeIdentifier node) {
        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node);
        return leafRefReferencingContext != null && leafRefReferencingContext.isReferencing();
    }

    @Beta
    public boolean hasLeafRefChild(final SchemaNodeIdentifier node) {
        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node);
        return leafRefReferencingContext != null && leafRefReferencingContext.hasReferencingChild();
    }

    @Beta
    public boolean isReferencedByLeafRef(final SchemaNodeIdentifier node) {
        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node);
        return leafRefReferencedByContext != null && leafRefReferencedByContext.isReferenced();
    }

    @Beta
    public boolean hasChildReferencedByLeafRef(final SchemaNodeIdentifier node) {
        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node);
        return leafRefReferencedByContext != null && leafRefReferencedByContext.hasReferencedChild();
    }

    @Beta
    public List<LeafRefContext> findAllLeafRefChilds(final SchemaNodeIdentifier node) {
        final LeafRefContext ctx = getLeafRefReferencingContext(node);
        return ctx == null ? List.of() : ctx.findAllLeafRefChilds();
    }

    private List<LeafRefContext> findAllLeafRefChilds() {
        if (isReferencing()) {
            return List.of(this);
        }

        final List<LeafRefContext> leafRefChilds = new ArrayList<>();
        for (final Entry<QName, LeafRefContext> child : getReferencingChilds().entrySet()) {
            leafRefChilds.addAll(child.getValue().findAllLeafRefChilds());
        }
        return leafRefChilds;
    }

    @Beta
    public List<LeafRefContext> findAllChildsReferencedByLeafRef(final SchemaNodeIdentifier node) {
        final LeafRefContext ctx = getLeafRefReferencedByContext(node);
        return ctx == null ? List.of() : ctx.findAllChildsReferencedByLeafRef();
    }

    private List<LeafRefContext> findAllChildsReferencedByLeafRef() {
        if (isReferenced()) {
            return List.of(this);
        }

        final List<LeafRefContext> childsReferencedByLeafRef = new ArrayList<>();
        for (final Entry<QName, LeafRefContext> child : getReferencedByChilds().entrySet()) {
            childsReferencedByLeafRef.addAll(child.getValue().findAllChildsReferencedByLeafRef());
        }
        return childsReferencedByLeafRef;
    }

    @Beta
    public Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(final SchemaNodeIdentifier node) {
        final LeafRefContext referencedByContext = getLeafRefReferencedByContext(node);
        return referencedByContext == null ? Map.of() : referencedByContext.getAllReferencedByLeafRefCtxs();
    }

    LeafRefPath getLeafRefNodePath() {
        LeafRefPath ret = leafRefNodePath;
        if (ret == null) {
            synchronized (this) {
                ret = leafRefNodePath;
                if (ret == null) {
                    ret = leafRefNodePath = LeafRefUtils.schemaPathToLeafRefPath(currentNodePath, module);
                }
            }
        }
        return ret;
    }
}
