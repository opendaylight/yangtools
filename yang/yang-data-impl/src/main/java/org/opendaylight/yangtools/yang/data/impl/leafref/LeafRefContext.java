/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

// FIXME: 3.0.0 hide this class
public final class LeafRefContext implements SchemaContextProvider {

    private final QName currentNodeQName;
    private final SchemaPath currentNodePath;
    private final SchemaContext schemaContext;
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
        this.currentNodeQName = leafRefContextBuilder.getCurrentNodeQName();
        this.currentNodePath = leafRefContextBuilder.getCurrentNodePath();
        this.schemaContext = leafRefContextBuilder.getSchemaContext();
        this.leafRefTargetPath = leafRefContextBuilder.getLeafRefTargetPath();
        this.absoluteLeafRefTargetPath = leafRefContextBuilder.getAbsoluteLeafRefTargetPath();
        this.leafRefTargetPathString = leafRefContextBuilder.getLeafRefTargetPathString();
        this.isReferencedBy = leafRefContextBuilder.isReferencedBy();
        this.isReferencing = leafRefContextBuilder.isReferencing();
        this.referencingChilds = ImmutableMap.copyOf(leafRefContextBuilder.getReferencingChilds());
        this.referencedByChilds = ImmutableMap.copyOf(leafRefContextBuilder.getReferencedByChilds());
        this.referencedByLeafRefCtx = ImmutableMap.copyOf(leafRefContextBuilder.getAllReferencedByLeafRefCtxs());
        this.module = leafRefContextBuilder.getLeafRefContextModule();
    }

    public static LeafRefContext create(final SchemaContext ctx) {
        try {
            return new LeafRefContextTreeBuilder(ctx).buildLeafRefContextTree();
        } catch (LeafRefYangSyntaxErrorException e) {
            throw new IllegalArgumentException(e);
        }
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

    public SchemaPath getCurrentNodePath() {
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

    @Override
    public SchemaContext getSchemaContext() {
        return schemaContext;
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
