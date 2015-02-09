/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;


import org.opendaylight.yangtools.yang.common.QNameModule;

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

    private LeafRefPath leafRefTargetPath = null;
    private LeafRefPath absoluteLeafRefTargetPath = null;
    private String leafRefTargetPathString = "";

    private boolean isReferencedBy = false;
    private boolean isReferencing = false;

    private LeafRefContext parent;
    private Map<QName, LeafRefContext> referencingChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<QName, LeafRefContext>();


    public LeafRefContextImpl(QName currentNodeQName, SchemaPath currentNodePath, SchemaContext schemaContext, LeafRefContext parent) {
        this.currentNodeQName = currentNodeQName;
        this.currentNodePath = currentNodePath;
        this.schemaContext = schemaContext;
        this.parent = parent;
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
    public void setReferencedBy(boolean isReferencedBy) {
        this.isReferencedBy = isReferencedBy;
    }

    @Override
    public boolean isReferencing() {
        return isReferencing;
    }

    @Override
    public void setReferencing(boolean isReferencing) {
        this.isReferencing = isReferencing;
    }

    @Override
    public void addReferencingChild(LeafRefContext child, QName childQName) {
        referencingChilds.put(childQName, child);
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
    public void addReferencedByChild(LeafRefContext child, QName childQName) {
        referencedByChilds.put(childQName, child);
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
    public void setCurrentNodePath(SchemaPath currentNodePath) {
        this.currentNodePath = currentNodePath;
    }

    @Override
    public LeafRefPath getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    @Override
    public void setLeafRefTargetPath(LeafRefPath leafRefPath) {
        this.leafRefTargetPath = leafRefPath;
    }

    @Override
    public String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    @Override
    public void setLeafRefTargetPathString(String leafRefPathString) {
        this.leafRefTargetPathString = leafRefPathString;
    }

    @Override
    public QName getCurrentNodeQName() {
        return currentNodeQName;
    }

    @Override
    public void setCurrentNodeQName(QName currentNodeQName) {
        this.currentNodeQName = currentNodeQName;
    }

    @Override
    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public void setSchemaContext(SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    @Override
    public LeafRefContext getParent() {
        return parent;
    }

    @Override
    public void setParent(LeafRefContext parent) {
        this.parent = parent;
    }

    @Override
    public LeafRefPath getAbsoluteLeafRefTargetPath() {

        if (absoluteLeafRefTargetPath == null) {
            if (leafRefTargetPath.isAbsolute()) {
                absoluteLeafRefTargetPath = leafRefTargetPath;
            } else {
                absoluteLeafRefTargetPath = LeafRefUtils
                        .createAbsoluteLeafRefPath(leafRefTargetPath,
                                currentNodePath, getLeafRefContextModule());
            }
        }

        return absoluteLeafRefTargetPath;
    }

    @Override
    public Module getLeafRefContextModule() {
        QNameModule qnameModule = currentNodeQName.getModule();

        return schemaContext.findModuleByNamespaceAndRevision(
                qnameModule.getNamespace(), qnameModule.getRevision());
    }


    @Override
    public void addReferencedByLeafRefCtx(QName qname, LeafRefContext leafRef) {
        referencedByLeafRefCtx.put(qname, leafRef);
    }

    @Override
    public void getReferencedByLeafRefCtxByName(QName qname) {
        referencedByLeafRefCtx.get(qname);
    }

}
