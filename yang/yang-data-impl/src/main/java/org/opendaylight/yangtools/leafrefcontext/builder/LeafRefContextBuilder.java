/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.builder;

import org.opendaylight.yangtools.leafrefcontext.impl.LeafRefContextImpl;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;
import org.opendaylight.yangtools.leafrefcontext.utils.LeafRefUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class LeafRefContextBuilder {

    private QName currentNodeQName;
    private SchemaPath currentNodePath;
    private SchemaContext schemaContext;

    private LeafRefPath leafRefTargetPath = null;
    private LeafRefPath absoluteLeafRefTargetPath = null;
    private String leafRefTargetPathString = "";

    private boolean isReferencedBy = false;
    private boolean isReferencing = false;

    private Map<QName, LeafRefContext> referencingChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<QName, LeafRefContext>();

    public LeafRefContextBuilder(QName currentNodeQName,
            SchemaPath currentNodePath, SchemaContext schemaContext) {
        this.currentNodeQName = currentNodeQName;
        this.currentNodePath = currentNodePath;
        this.schemaContext = schemaContext;
    }

    public LeafRefContext build() {
        LeafRefContext leafRefContext = new LeafRefContextImpl(this);

        referencingChilds = new HashMap<QName, LeafRefContext>();
        referencedByChilds = new HashMap<QName, LeafRefContext>();
        referencedByLeafRefCtx = new HashMap<QName, LeafRefContext>();

        return leafRefContext;
    }

    public boolean hasLeafRefContextChild() {
        return hasReferencedByChild() || hasReferencingChild();
    }

    public boolean hasReferencedByChild() {
        return !referencedByChilds.isEmpty();
    }

    public boolean hasReferencingChild() {
        return !referencingChilds.isEmpty();
    }

    public boolean isReferencedBy() {
        return isReferencedBy;
    }

    public void setReferencedBy(boolean isReferencedBy) {
        this.isReferencedBy = isReferencedBy;
    }

    public boolean isReferencing() {
        return isReferencing;
    }

    public void setReferencing(boolean isReferencing) {
        this.isReferencing = isReferencing;
    }

    public void addReferencingChild(LeafRefContext child, QName childQName) {
        referencingChilds.put(childQName, child);
    }

    public LeafRefContext getReferencingChildByName(QName name) {
        return referencingChilds.get(name);
    }

    public Map<QName, LeafRefContext> getReferencingChilds() {
        return referencingChilds;
    }

    public void addReferencedByChild(LeafRefContext child, QName childQName) {
        referencedByChilds.put(childQName, child);
    }

    public LeafRefContext getReferencedByChildByName(QName name) {
        return referencedByChilds.get(name);
    }

    public Map<QName, LeafRefContext> getReferencedByChilds() {
        return referencedByChilds;
    }

    public SchemaPath getCurrentNodePath() {
        return currentNodePath;
    }

    public void setCurrentNodePath(SchemaPath currentNodePath) {
        this.currentNodePath = currentNodePath;
    }

    public LeafRefPath getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    public void setLeafRefTargetPath(LeafRefPath leafRefPath) {
        this.leafRefTargetPath = leafRefPath;
    }

    public String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    public void setLeafRefTargetPathString(String leafRefPathString) {
        this.leafRefTargetPathString = leafRefPathString;
    }

    public QName getCurrentNodeQName() {
        return currentNodeQName;
    }

    public void setCurrentNodeQName(QName currentNodeQName) {
        this.currentNodeQName = currentNodeQName;
    }

    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public void setSchemaContext(SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    public LeafRefPath getAbsoluteLeafRefTargetPath() {

        if (isReferencing && absoluteLeafRefTargetPath == null) {
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

    public Module getLeafRefContextModule() {
        QNameModule qnameModule = currentNodeQName.getModule();

        return schemaContext.findModuleByNamespaceAndRevision(
                qnameModule.getNamespace(), qnameModule.getRevision());
    }

    public void addReferencedByLeafRefCtx(QName qname, LeafRefContext leafRef) {
        referencedByLeafRefCtx.put(qname, leafRef);
    }

    public LeafRefContext getReferencedByLeafRefCtxByName(QName qname) {
        return referencedByLeafRefCtx.get(qname);
    }

    public Map<QName, LeafRefContext> getAllReferencedByLeafRefCtxs() {
        return referencedByLeafRefCtx;
    }

}
