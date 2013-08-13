/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AugmentationSchemaBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.TypeDefinitionBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UsesNodeBuilderImpl;

public class CopyUtils {

    /**
     * Create copy of DataSchemaNodeBuilder with new parent. If updateQName is
     * true, qname of node will be corrected based on new parent.
     *
     * @param old
     * @param newParent
     * @param updateQName
     * @return
     */
    public static DataSchemaNodeBuilder copy(DataSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        if (old instanceof AnyXmlBuilder) {
            return copy((AnyXmlBuilder) old, newParent, updateQName);
        } else if (old instanceof ChoiceBuilder) {
            return copy((ChoiceBuilder) old, newParent, updateQName);
        } else if (old instanceof ContainerSchemaNodeBuilder) {
            return copy((ContainerSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof LeafSchemaNodeBuilder) {
            return copy((LeafSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof LeafListSchemaNodeBuilder) {
            return copy((LeafListSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof ListSchemaNodeBuilder) {
            return copy((ListSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof ChoiceCaseBuilder) {
            return copy((ChoiceCaseBuilder) old, newParent, updateQName);
        } else {
            throw new YangParseException(old.getModuleName(), old.getLine(),
                    "Failed to copy node: Unknown type of DataSchemaNode: " + old);
        }
    }

    private static AnyXmlBuilder copy(AnyXmlBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        AnyXmlBuilder c = new AnyXmlBuilder(newParent.getModuleName(), newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAugmenting(old.isAugmenting());
        c.setAddedByUses(old.isAddedByUses());
        c.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    private static ChoiceBuilder copy(ChoiceBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ChoiceBuilder c = new ChoiceBuilder(newParent.getModuleName(), newParent.getLine(), newQName);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAugmenting(old.isAugmenting());
        c.setAddedByUses(old.isAddedByUses());
        c.setConfiguration(old.isConfiguration());
        for (ChoiceCaseBuilder childNode : old.getCases()) {
            c.addCase(copy(childNode, c, updateQName));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentations()) {
            c.addAugmentation(copyAugment(augment, c));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    private static ChoiceCaseBuilder copy(ChoiceCaseBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ChoiceCaseBuilder c = new ChoiceCaseBuilder(newParent.getModuleName(), newParent.getLine(), newQName);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAugmenting(old.isAugmenting());
        c.getChildNodes().addAll(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            c.addChildNode(copy(childNode, c, updateQName));
        }
        c.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            c.addGrouping(copy(grouping, c, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            c.addTypedef(copy(tdb, c, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            c.addUsesNode(copyUses(oldUses, c));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    private static ContainerSchemaNodeBuilder copy(ContainerSchemaNodeBuilder old, Builder newParent,
            boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ContainerSchemaNodeBuilder c = new ContainerSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setPresence(old.isPresence());
        c.setAugmenting(old.isAugmenting());
        c.setAddedByUses(old.isAddedByUses());
        c.setConfiguration(old.isConfiguration());
        c.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            c.addChildNode(copy(childNode, c, updateQName));
        }
        c.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            c.addGrouping(copy(grouping, c, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            c.addTypedef(copy(tdb, c, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            c.addUsesNode(copyUses(oldUses, c));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentations()) {
            c.addAugmentation(copyAugment(augment, c));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    private static LeafSchemaNodeBuilder copy(LeafSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        LeafSchemaNodeBuilder c = new LeafSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(), newQName,
                newSchemaPath);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAugmenting(old.isAugmenting());
        c.setAddedByUses(old.isAddedByUses());
        c.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        if (old.getType() == null) {
            c.setTypedef(copy(old.getTypedef(), c, updateQName));
        } else {
            c.setType(old.getType());
        }

        c.setDefaultStr(old.getDefaultStr());
        c.setUnits(old.getUnits());

        return c;
    }

    public static LeafListSchemaNodeBuilder copy(LeafListSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        LeafListSchemaNodeBuilder c = new LeafListSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAugmenting(old.isAugmenting());
        c.setAddedByUses(old.isAddedByUses());
        c.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        if (old.getType() == null) {
            c.setTypedef(copy(old.getTypedef(), c, updateQName));
        } else {
            c.setType(old.getType());
        }

        c.setUserOrdered(old.isUserOrdered());

        return c;
    }

    private static ListSchemaNodeBuilder copy(ListSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ListSchemaNodeBuilder c = new ListSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(), newQName,
                newSchemaPath);
        copyConstraints(c.getConstraints(), old.getConstraints());
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAugmenting(old.isAugmenting());
        c.setAddedByUses(old.isAddedByUses());
        c.setConfiguration(old.isConfiguration());
        c.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            c.addChildNode(copy(childNode, c, updateQName));
        }
        c.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            c.addGrouping(copy(grouping, c, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            c.addTypedef(copy(tdb, c, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            c.addUsesNode(copyUses(oldUses, c));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentations()) {
            c.addAugmentation(copyAugment(augment, c));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        c.setUserOrdered(old.isUserOrdered());
        c.setKeyDefinition(old.getKeyDefinition());

        return c;
    }

    static GroupingBuilder copy(GroupingBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        GroupingBuilderImpl c = new GroupingBuilderImpl(newParent.getModuleName(), newParent.getLine(), newQName);
        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAddedByUses(old.isAddedByUses());
        c.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            c.addChildNode(copy(childNode, c, updateQName));
        }
        c.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            c.addGrouping(copy(grouping, c, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            c.addTypedef(copy(tdb, c, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            c.addUsesNode(copyUses(oldUses, c));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    static TypeDefinitionBuilder copy(TypeDefinitionBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;
        TypeDefinitionBuilder type = null;

        if (old instanceof UnionTypeBuilder) {
            type = new UnionTypeBuilder(newParent.getModuleName(), newParent.getLine());
        } else if (old instanceof IdentityrefTypeBuilder) {
            type = new IdentityrefTypeBuilder(newParent.getModuleName(), newParent.getLine(),
                    ((IdentityrefTypeBuilder) old).getBaseString(), newSchemaPath);
        } else {
            type = new TypeDefinitionBuilderImpl(old.getModuleName(), newParent.getLine(), newQName);
            type.setParent(newParent);
            type.setPath(newSchemaPath);

            if (old.getType() == null) {
                type.setTypedef(copy(old.getTypedef(), type, updateQName));
            } else {
                type.setType(old.getType());
            }

            for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
                type.addUnknownNodeBuilder((copy(un, type, updateQName)));
            }

            type.setRanges(old.getRanges());
            type.setLengths(old.getLengths());
            type.setPatterns(old.getPatterns());
            type.setFractionDigits(old.getFractionDigits());
            type.setDescription(old.getDescription());
            type.setReference(old.getReference());
            type.setStatus(old.getStatus());
            type.setUnits(old.getUnits());
            type.setDefaultValue(old.getDefaultValue());
            type.setAddedByUses(old.isAddedByUses());
        }

        return type;
    }

    private static ConstraintsBuilder copyConstraints(ConstraintsBuilder newConstraints, ConstraintsBuilder old) {
        newConstraints.getMustDefinitions().addAll(old.getMustDefinitions());
        newConstraints.addWhenCondition(old.getWhenCondition());
        newConstraints.setMandatory(old.isMandatory());
        newConstraints.setMinElements(old.getMinElements());
        newConstraints.setMaxElements(old.getMaxElements());
        return newConstraints;
    }

    static UsesNodeBuilder copyUses(UsesNodeBuilder old, Builder newParent) {
        UsesNodeBuilder u = new UsesNodeBuilderImpl(newParent.getModuleName(), newParent.getLine(),
                old.getGroupingName());
        u.setParent(newParent);
        u.setGroupingPath(old.getGroupingPath());
        u.setAugmenting(old.isAugmenting());
        u.setAddedByUses(old.isAddedByUses());
        u.getAugmentations().addAll(old.getAugmentations());
        u.getRefineNodes().addAll(old.getRefineNodes());
        u.getRefines().addAll(old.getRefines());
        u.getFinalChildren().addAll(old.getFinalChildren());
        u.getFinalGroupings().addAll(old.getFinalGroupings());
        u.getFinalTypedefs().addAll(old.getFinalTypedefs());
        u.getFinalUnknownNodes().addAll(old.getFinalUnknownNodes());

        Set<DataSchemaNodeBuilder> oldChildren = old.getTargetChildren();
        Set<DataSchemaNodeBuilder> newChildren = new HashSet<>();
        if (oldChildren != null) {
            for (DataSchemaNodeBuilder child : old.getTargetChildren()) {
                newChildren.add(CopyUtils.copy(child, newParent, true));
            }
        }
        u.setTargetChildren(newChildren);

        Set<TypeDefinitionBuilder> oldTypedefs = old.getTargetTypedefs();
        Set<TypeDefinitionBuilder> newTypedefs = new HashSet<>();
        if (oldTypedefs != null) {
            for (TypeDefinitionBuilder typedef : old.getTargetTypedefs()) {
                newTypedefs.add(CopyUtils.copy(typedef, newParent, true));
            }
        }
        u.setTargetTypedefs(newTypedefs);

        Set<GroupingBuilder> oldGroupings = old.getTargetGroupings();
        Set<GroupingBuilder> newGroupings = new HashSet<>();
        if (oldGroupings != null) {
            for (GroupingBuilder grouping : old.getTargetGroupings()) {
                newGroupings.add(copy(grouping, newParent, true));
            }
        }
        u.setTargetGroupings(newGroupings);

        List<UnknownSchemaNodeBuilder> oldUN = old.getTargetUnknownNodes();
        List<UnknownSchemaNodeBuilder> newUN = new ArrayList<>();
        if (oldUN != null) {
            for (UnknownSchemaNodeBuilder un : oldUN) {
                newUN.add(copy(un, newParent, true));
            }
        }
        u.setTargetUnknownNodes(newUN);

        for (UsesNodeBuilder uses : old.getTargetGroupingUses()) {
            u.getTargetGroupingUses().add(copyUses(uses, uses.getParent()));
        }

        // add new uses to collection of uses in module
        ModuleBuilder module = ParserUtils.getParentModule(newParent);
        module.addUsesNode(u);

        return u;
    }

    private static AugmentationSchemaBuilder copyAugment(AugmentationSchemaBuilder old, Builder newParent) {
        AugmentationSchemaBuilderImpl a = new AugmentationSchemaBuilderImpl(newParent.getModuleName(),
                newParent.getLine(), old.getTargetPathAsString());
        a.setParent(newParent);

        a.setDescription(old.getDescription());
        a.setReference(old.getReference());
        a.setStatus(old.getStatus());
        a.addWhenCondition(old.getWhenCondition());
        a.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            a.addChildNode(copy(childNode, a, false));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            a.addUsesNode(copyUses(oldUses, a));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            a.addUnknownNodeBuilder((copy(un, a, false)));
        }

        return a;
    }

    static UnknownSchemaNodeBuilder copy(UnknownSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        UnknownSchemaNodeBuilder c = new UnknownSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName);

        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAddedByUses(old.isAddedByUses());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    private static DataBean getdata(SchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        List<QName> newPath = null;
        QName newQName = null;
        if (newParent instanceof ModuleBuilder) {
            ModuleBuilder parent = (ModuleBuilder) newParent;
            if (updateQName) {
                newQName = new QName(parent.getNamespace(), parent.getRevision(), parent.getPrefix(), old.getQName()
                        .getLocalName());
                newPath = Collections.singletonList(newQName);
            } else {
                newQName = old.getQName();
                newPath = Collections.singletonList(newQName);
            }
        } else if (newParent instanceof AugmentationSchemaBuilder) {
            ModuleBuilder parent = ParserUtils.getParentModule(newParent);
            if (updateQName) {
                newQName = new QName(parent.getNamespace(), parent.getRevision(), parent.getPrefix(), old.getQName()
                        .getLocalName());
                newPath = Collections.singletonList(newQName);
            } else {
                newQName = old.getQName();
                newPath = Collections.singletonList(newQName);
            }

        } else if (newParent instanceof SchemaNodeBuilder) {
            SchemaNodeBuilder parent = (SchemaNodeBuilder) newParent;
            QName parentQName = parent.getQName();
            if (updateQName) {
                newQName = new QName(parentQName.getNamespace(), parentQName.getRevision(), parentQName.getPrefix(),
                        old.getQName().getLocalName());
                newPath = new ArrayList<>(parent.getPath().getPath());
                newPath.add(newQName);
            } else {
                newQName = old.getQName();
                newPath = new ArrayList<>(parent.getPath().getPath());
                newPath.add(newQName);
            }
        }

        SchemaPath newSchemaPath = new SchemaPath(newPath, true);
        return new DataBean(newQName, newSchemaPath);
    }

    private static class DataBean {
        private QName qname;
        private SchemaPath schemaPath;

        private DataBean(QName qname, SchemaPath schemaPath) {
            this.qname = qname;
            this.schemaPath = schemaPath;
        }
    }

}
