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
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.*;

public final class CopyUtils {

    private CopyUtils() {
    }

    /**
     * Create copy of DataSchemaNodeBuilder with new parent. If updateQName is
     * true, qname of node will be corrected based on new parent.
     *
     * @param old
     *            builder to copy
     * @param newParent
     *            new parent
     * @param updateQName
     *            flag to indicate if qname should be updated based on new
     *            parent location
     * @return copy of given builder
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

        AnyXmlBuilder copy = new AnyXmlBuilder(newParent.getModuleName(), newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static ChoiceBuilder copy(ChoiceBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ChoiceBuilder copy = new ChoiceBuilder(newParent.getModuleName(), newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (ChoiceCaseBuilder childNode : old.getCases()) {
            copy.addCase(copy(childNode, copy, updateQName));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentationBuilders()) {
            copy.addAugmentation(copyAugment(augment, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static ChoiceCaseBuilder copy(ChoiceCaseBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ChoiceCaseBuilder copy = new ChoiceCaseBuilder(newParent.getModuleName(), newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodeBuilders()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static ContainerSchemaNodeBuilder copy(ContainerSchemaNodeBuilder old, Builder newParent,
            boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ContainerSchemaNodeBuilder copy = new ContainerSchemaNodeBuilder(newParent.getModuleName(),
                newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setPresence(old.isPresence());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodeBuilders()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentationBuilders()) {
            copy.addAugmentation(copyAugment(augment, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static LeafSchemaNodeBuilder copy(LeafSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        LeafSchemaNodeBuilder copy = new LeafSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        if (old.getType() == null) {
            copy.setTypedef(copy(old.getTypedef(), copy, updateQName));
        } else {
            copy.setType(old.getType());
        }

        copy.setDefaultStr(old.getDefaultStr());
        copy.setUnits(old.getUnits());

        return copy;
    }

    public static LeafListSchemaNodeBuilder copy(LeafListSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        LeafListSchemaNodeBuilder copy = new LeafListSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        if (old.getType() == null) {
            copy.setTypedef(copy(old.getTypedef(), copy, updateQName));
        } else {
            copy.setType(old.getType());
        }

        copy.setUserOrdered(old.isUserOrdered());

        return copy;
    }

    private static ListSchemaNodeBuilder copy(ListSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ListSchemaNodeBuilder copy = new ListSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodeBuilders()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentationBuilders()) {
            copy.addAugmentation(copyAugment(augment, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        copy.setUserOrdered(old.isUserOrdered());
        copy.setKeys(old.getKeys());

        return copy;
    }

    public static GroupingBuilder copy(GroupingBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        GroupingBuilderImpl copy = new GroupingBuilderImpl(newParent.getModuleName(), newParent.getLine(), newQName, newSchemaPath);
        copy.setParent(newParent);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAddedByUses(old.isAddedByUses());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodeBuilders()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    public static TypeDefinitionBuilder copy(TypeDefinitionBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;
        TypeDefinitionBuilder type;

        if (old instanceof UnionTypeBuilder) {
            UnionTypeBuilder oldUnion = (UnionTypeBuilder) old;
            type = new UnionTypeBuilder(newParent.getModuleName(), newParent.getLine());
            type.setParent(newParent);
            for (TypeDefinition<?> td : oldUnion.getTypes()) {
                type.setType(td);
            }
            for (TypeDefinitionBuilder tdb : oldUnion.getTypedefs()) {
                type.setTypedef(copy(tdb, type, updateQName));
            }
        } else if (old instanceof IdentityrefTypeBuilder) {
            type = new IdentityrefTypeBuilder(newParent.getModuleName(), newParent.getLine(),
                    ((IdentityrefTypeBuilder) old).getBaseString(), newSchemaPath);
            type.setParent(newParent);
        } else {
            type = new TypeDefinitionBuilderImpl(old.getModuleName(), newParent.getLine(), newQName, old.getPath());
            type.setParent(newParent);

            if (old.getType() == null) {
                type.setTypedef(copy(old.getTypedef(), type, updateQName));
            } else {
                type.setType(old.getType());
            }

            for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
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

    private static UsesNodeBuilder copyUses(UsesNodeBuilder old, Builder newParent) {
        UsesNodeBuilder copy = new UsesNodeBuilderImpl(newParent.getModuleName(), newParent.getLine(),
                old.getGroupingPathAsString());
        copy.setParent(newParent);
        copy.setGroupingDefinition(old.getGroupingDefinition());
        copy.setGrouping(old.getGroupingBuilder());
        copy.setAddedByUses(old.isAddedByUses());
        copy.getAugmentations().addAll(old.getAugmentations());
        copy.getRefineNodes().addAll(old.getRefineNodes());
        copy.getRefines().addAll(old.getRefines());
        copy.setAugmenting(old.isAugmenting());
        return copy;
    }

    private static AugmentationSchemaBuilder copyAugment(AugmentationSchemaBuilder old, Builder newParent) {
        AugmentationSchemaBuilderImpl copy = new AugmentationSchemaBuilderImpl(newParent.getModuleName(),
                newParent.getLine(), old.getTargetPathAsString());
        copy.setParent(newParent);

        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.addWhenCondition(old.getWhenCondition());
        copy.setTargetNodeSchemaPath(old.getTargetNodeSchemaPath());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, false));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodeBuilders()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            copy.addUnknownNodeBuilder((copy(un, copy, false)));
        }

        return copy;
    }

    public static UnknownSchemaNodeBuilder copy(UnknownSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        UnknownSchemaNodeBuilder c = new UnknownSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);

        c.setNodeType(old.getNodeType());
        c.setNodeParameter(old.getNodeParameter());
        c.setParent(newParent);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAddedByUses(old.isAddedByUses());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodes()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }
        c.setExtensionBuilder(old.getExtensionBuilder());
        c.setExtensionDefinition(old.getExtensionDefinition());

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
            AugmentationSchemaBuilder augment = (AugmentationSchemaBuilder) newParent;
            ModuleBuilder parent = ParserUtils.getParentModule(newParent);
            if (updateQName) {
                newQName = new QName(parent.getNamespace(), parent.getRevision(), parent.getPrefix(), old.getQName()
                        .getLocalName());
                newPath = new ArrayList<>(augment.getTargetNodeSchemaPath().getPath());
                newPath.add(newQName);
            } else {
                newQName = old.getQName();
                newPath = new ArrayList<>(augment.getTargetNodeSchemaPath().getPath());
                newPath.add(newQName);
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

    private static final class DataBean {
        private QName qname;
        private SchemaPath schemaPath;

        private DataBean(QName qname, SchemaPath schemaPath) {
            this.qname = qname;
            this.schemaPath = schemaPath;
        }
    }

}
