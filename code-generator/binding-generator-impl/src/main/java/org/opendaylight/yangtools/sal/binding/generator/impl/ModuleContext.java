/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public final class ModuleContext {
    private GeneratedTypeBuilder moduleNode;
    private final List<GeneratedTOBuilder> genTOs = new ArrayList<GeneratedTOBuilder>();
    private final Map<SchemaPath, Type> typedefs = new HashMap<SchemaPath, Type>();
    private final Map<SchemaPath, GeneratedTypeBuilder> childNodes = new HashMap<SchemaPath, GeneratedTypeBuilder>();
    private final Map<SchemaPath, GeneratedTypeBuilder> groupings = new HashMap<SchemaPath, GeneratedTypeBuilder>();
    private final Map<SchemaPath, GeneratedTypeBuilder> cases = new HashMap<SchemaPath, GeneratedTypeBuilder>();
    private final Map<QName,GeneratedTOBuilder> identities = new HashMap<>();
    private final Set<GeneratedTypeBuilder> topLevelNodes = new HashSet<GeneratedTypeBuilder>();
    private final List<GeneratedTypeBuilder> augmentations = new ArrayList<GeneratedTypeBuilder>();
    private final BiMap<Type,AugmentationSchema> typeToAugmentation = HashBiMap.create();

    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();

    private final Multimap<Type, Type> augmentableToAugmentations = HashMultimap.create();

    List<Type> getGeneratedTypes() {
        List<Type> result = new ArrayList<>();

        if (moduleNode != null) {
            result.add(moduleNode.toInstance());
        }

        for (GeneratedTOBuilder b : genTOs) {
            result.add(b.toInstance());
        }
        for (Type b : typedefs.values()) {
            if (b != null) {
                result.add(b);
            }
        }
        for (GeneratedTypeBuilder b : childNodes.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : groupings.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : cases.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTOBuilder b : identities.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : topLevelNodes) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : augmentations) {
            result.add(b.toInstance());
        }
        return result;
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return choiceToCases;
    }

    public Multimap<Type, Type> getAugmentableToAugmentations() {
        return augmentableToAugmentations;
    }

    public GeneratedTypeBuilder getModuleNode() {
        return moduleNode;
    }

    public GeneratedTypeBuilder getChildNode(final SchemaPath p) {
        return childNodes.get(p);
    }

    public GeneratedTypeBuilder getGrouping(final SchemaPath p) {
        return groupings.get(p);
    }

    public GeneratedTypeBuilder getCase(final SchemaPath p) {
        return cases.get(p);
    }

    public void addModuleNode(final GeneratedTypeBuilder moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void addGeneratedTOBuilder(final GeneratedTOBuilder b) {
        genTOs.add(b);
    }

    public void addChildNodeType(final SchemaPath p, final GeneratedTypeBuilder b) {
        childNodes.put(p, b);
    }

    public void addGroupingType(final SchemaPath p, final GeneratedTypeBuilder b) {
        groupings.put(p, b);
    }

    public void addTypedefType(final SchemaPath p, final Type t) {
        typedefs.put(p, t);
    }

    public void addCaseType(final SchemaPath p, final GeneratedTypeBuilder b) {
        cases.put(p, b);
    }

    public void addIdentityType(final QName name,final GeneratedTOBuilder b) {
        identities.put(name,b);
    }

    public void addTopLevelNodeType(final GeneratedTypeBuilder b) {
        topLevelNodes.add(b);
    }

    public void addAugmentType(final GeneratedTypeBuilder b) {
        augmentations.add(b);
    }

    public Map<SchemaPath, Type> getTypedefs() {
        return typedefs;
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getChildNodes() {
        return childNodes;
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getGroupings() {
        return groupings;
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getCases() {
        return cases;
    }

    public Map<QName,GeneratedTOBuilder> getIdentities() {
        return identities;
    }

    public Set<GeneratedTypeBuilder> getTopLevelNodes() {
        return topLevelNodes;
    }

    public List<GeneratedTypeBuilder> getAugmentations() {
        return augmentations;
    }

    public BiMap<Type, AugmentationSchema> getTypeToAugmentation() {
        return typeToAugmentation;
    }

    public void addTypeToAugmentation(final GeneratedTypeBuilder builder, final AugmentationSchema schema) {
        typeToAugmentation.put(builder, schema);
    }

    public void addTargetToAugmentation(final Type target, final GeneratedTypeBuilder augmentation) {
        augmentableToAugmentations.put(target,augmentation);
    }

}
