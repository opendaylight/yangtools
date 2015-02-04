/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.impl;

import org.opendaylight.yangtools.leafrefcontext.builder.LeafRefContextBuilder;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.util.HashMap;
import org.opendaylight.yangtools.yang.common.QName;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class LeafRefContextImpl implements LeafRefContext {

    private QName currentNodeQName;
    private SchemaPath currentNodePath;
    private SchemaContext schemaContext;
    private Module module;

    private LeafRefPath leafRefTargetPath = null;
    private LeafRefPath absoluteLeafRefTargetPath = null;
    private String leafRefTargetPathString = "";

    private boolean isReferencedBy = false;
    private boolean isReferencing = false;

    private Map<QName, LeafRefContext> referencingChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<QName, LeafRefContext>();

    public LeafRefContextImpl(QName currentNodeQName,
            SchemaPath currentNodePath, SchemaContext schemaContext) {
        this.currentNodeQName = currentNodeQName;
        this.currentNodePath = currentNodePath;
        this.schemaContext = schemaContext;
    }

    public LeafRefContextImpl(LeafRefContextBuilder leafRefContextBuilder) {
        this.currentNodeQName = leafRefContextBuilder.getCurrentNodeQName();
        this.currentNodePath = leafRefContextBuilder.getCurrentNodePath();
        this.schemaContext = leafRefContextBuilder.getSchemaContext();
        this.leafRefTargetPath = leafRefContextBuilder.getLeafRefTargetPath();
        this.absoluteLeafRefTargetPath = leafRefContextBuilder
                .getAbsoluteLeafRefTargetPath();
        this.leafRefTargetPathString = leafRefContextBuilder
                .getLeafRefTargetPathString();
        this.isReferencedBy = leafRefContextBuilder.isReferencedBy();
        this.isReferencing = leafRefContextBuilder.isReferencing();
        this.referencingChilds = leafRefContextBuilder.getReferencingChilds();
        this.referencedByChilds = leafRefContextBuilder.getReferencedByChilds();
        this.referencedByLeafRefCtx = leafRefContextBuilder
                .getAllReferencedByLeafRefCtxs();
        this.module = leafRefContextBuilder.getLeafRefContextModule();
    }

    @Override
    public boolean hasLeafRefContextChild() {
        return hasReferencedByChild() || hasReferencingChild();
    }

    @Override
    public boolean hasReferencedByChild() {
        return !referencedByChilds.isEmpty();
    }

    @Override
    public boolean hasReferencingChild() {
        return !referencingChilds.isEmpty();
    }

    @Override
    public boolean isReferencedBy() {
        return isReferencedBy;
    }

    @Override
    public boolean isReferencing() {
        return isReferencing;
    }

    @Override
    public LeafRefContext getReferencingChildByName(QName name) {
        return referencingChilds.get(name);
    }

    @Override
    public Map<QName, LeafRefContext> getReferencingChilds() {
        return referencingChilds;
    }

    @Override
    public LeafRefContext getReferencedByChildByName(QName name) {
        return referencedByChilds.get(name);
    }

    @Override
    public Map<QName, LeafRefContext> getReferencedByChilds() {
        return referencedByChilds;
    }

    @Override
    public SchemaPath getCurrentNodePath() {
        return currentNodePath;
    }

    @Override
    public LeafRefPath getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    @Override
    public String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    @Override
    public QName getCurrentNodeQName() {
        return currentNodeQName;
    }

    @Override
    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public LeafRefPath getAbsoluteLeafRefTargetPath() {
        return absoluteLeafRefTargetPath;
    }

    @Override
    public Module getLeafRefContextModule() {
        return module;
    }

    @Override
    public LeafRefContext getReferencedByLeafRefCtxByName(QName qname) {
        return referencedByLeafRefCtx.get(qname);
    }

    @Override
    public Map<QName, LeafRefContext> getAllReferencedByLeafRefCtxs() {
        return referencedByLeafRefCtx;
    }

}
