/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import java.util.Collection
import java.util.List
import java.util.Map
import java.util.Set
import org.opendaylight.mdsal.binding.model.util.FormattingUtils
import org.opendaylight.yangtools.yang.common.Revision
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.Deviation
import org.opendaylight.yangtools.yang.model.api.DocumentedNode
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.ModuleImport
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import org.opendaylight.yangtools.yang.model.api.Status
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode
import org.opendaylight.yangtools.yang.model.api.UsesNode
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair

final class YangTemplate {

    private static val String SKIP_PROPERTY_NAME = "mdsal.skip.verbose"

    private static val SKIP = Boolean.getBoolean(SKIP_PROPERTY_NAME)

    private static val SKIPPED_EMPTY = '''(Empty due to «SKIP_PROPERTY_NAME» property = true)'''

    def static String generateYangSnippet(Module module) {
        if (SKIP)
            return SKIPPED_EMPTY
        '''
            module «module.name» {
                yang-version «module.yangVersion»;
                namespace "«module.QNameModule.namespace.toString»";
                prefix "«module.prefix»";

                «IF !module.imports.nullOrEmpty»
                «writeModuleImports(module.imports)»
                «ENDIF»
                «IF module.revision.present»
                «writeRevision(module.revision.get, module.description.orElse(null))»
                «ENDIF»
                «IF !module.childNodes.nullOrEmpty»

                «writeDataSchemaNodes(module.childNodes)»
                «ENDIF»
                «IF !module.groupings.nullOrEmpty»

                «writeGroupingDefs(module.groupings)»
                «ENDIF»
                «IF !module.augmentations.nullOrEmpty»

                «writeAugments(module.augmentations)»
                «ENDIF»
                «IF !module.deviations.nullOrEmpty»

                «writeDeviations(module.deviations)»
                «ENDIF»
                «IF !module.extensionSchemaNodes.nullOrEmpty»

                «writeExtensions(module.extensionSchemaNodes)»
                «ENDIF»
                «IF !module.features.nullOrEmpty»

                «writeFeatures(module.features)»
                «ENDIF»
                «IF !module.identities.nullOrEmpty»

                «writeIdentities(module.identities)»
                «ENDIF»
                «IF !module.notifications.nullOrEmpty»

                «writeNotifications(module.notifications)»
                «ENDIF»
                «IF !module.rpcs.nullOrEmpty»

                «writeRPCs(module.rpcs)»
                «ENDIF»
                «IF !module.unknownSchemaNodes.nullOrEmpty»

                «writeUnknownSchemaNodes(module.unknownSchemaNodes)»
                «ENDIF»
                «IF !module.uses.nullOrEmpty»

                «writeUsesNodes(module.uses)»
                «ENDIF»
            }
        '''
    }

    def static String generateYangSnippet(DocumentedNode schemaNode) {
        if (schemaNode === null)
            return ''
        if (schemaNode instanceof Module)
            return generateYangSnippet(schemaNode)
        if (SKIP)
            return SKIPPED_EMPTY
        '''
            «IF schemaNode instanceof DataSchemaNode»
            «writeDataSchemaNode(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof EnumTypeDefinition.EnumPair»
            «writeEnumPair(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof ExtensionDefinition»
            «writeExtension(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof FeatureDefinition»
            «writeFeature(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof GroupingDefinition»
            «writeGroupingDef(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof IdentitySchemaNode»
            «writeIdentity(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof NotificationDefinition»
            «writeNotification(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof RpcDefinition»
            «writeRPC(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof TypeDefinition<?>»
            «writeTypeDefinition(schemaNode)»
            «ENDIF»
            «IF schemaNode instanceof UnknownSchemaNode»
            «writeUnknownSchemaNode(schemaNode)»
            «ENDIF»
        '''
    }

    def private static writeEnumPair(EnumPair pair) {
        '''
            enum «pair.name» {
                value «pair.value»;
            }
        '''
    }

    def private static String writeModuleImports(Set<ModuleImport> moduleImports) {
        if (moduleImports.nullOrEmpty)
            return ''

        '''
            «FOR moduleImport : moduleImports SEPARATOR "\n"»
                «IF moduleImport !== null && !moduleImport.moduleName.nullOrEmpty»
                import «moduleImport.moduleName» { prefix "«moduleImport.prefix»"; }
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeRevision(Revision moduleRevision, String moduleDescription) {
        val revisionIndent = 12

        '''
            revision «moduleRevision.toString» {
                description "«FormattingUtils.formatToParagraph(moduleDescription, revisionIndent)»";
            }
        '''
    }

    def private static writeRPCs(Set<RpcDefinition> rpcDefs) {
        '''
            «FOR rpc : rpcDefs»
                «IF rpc !== null»
                «writeRPC(rpc)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeRPC(RpcDefinition rpc) {
        var boolean isStatusDeprecated = rpc.status == Status::DEPRECATED
        '''
            rpc «rpc.QName.localName» {
                «IF rpc.description.present»
                    "«rpc.description.get»";
                «ENDIF»
                «IF !rpc.groupings.nullOrEmpty»
                    «writeGroupingDefs(rpc.groupings)»
                «ENDIF»
                «IF rpc.input !== null»
                    «writeRpcInput(rpc.input)»
                «ENDIF»
                «IF rpc.output !== null»
                    «writeRpcOutput(rpc.output)»
                «ENDIF»
                «IF rpc.reference.present»
                reference
                    "«rpc.reference.get»";
                «ENDIF»
                «IF isStatusDeprecated»
                status «rpc.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeRpcInput(ContainerSchemaNode input) {
        if (input === null)
            return ''

        '''
            input {
                «IF !input.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(input.childNodes)»
                «ENDIF»
            }

        '''
    }

    def private static writeRpcOutput(ContainerSchemaNode output) {
        if (output === null)
            return ''

        '''
            output {
                «IF !output.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(output.childNodes)»
                «ENDIF»
            }
        '''
    }

    def private static writeNotifications(Set<NotificationDefinition> notifications) {
        '''
            «FOR notification : notifications»
                «IF notification !== null»
                «writeNotification(notification)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeNotification(NotificationDefinition notification) {
        var boolean isStatusDeprecated = notification.status == Status::DEPRECATED
        '''
            notification «notification.QName.localName» {
                «IF notification.description.present»
                description
                    "«notification.description.get»";
                «ENDIF»
                «IF !notification.childNodes.nullOrEmpty»
                    «writeDataSchemaNodes(notification.childNodes)»
                «ENDIF»
                «IF !notification.availableAugmentations.nullOrEmpty»
                    «writeAugments(notification.availableAugmentations)»
                «ENDIF»
                «IF !notification.groupings.nullOrEmpty»
                    «writeGroupingDefs(notification.groupings)»
                «ENDIF»
                «IF !notification.uses.nullOrEmpty»
                    «writeUsesNodes(notification.uses)»
                «ENDIF»
                «IF notification.reference.present»
                reference
                    "«notification.reference.get»";
                «ENDIF»
                «IF isStatusDeprecated»
                status «notification.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
        if (unknownSchemaNodes.nullOrEmpty)
            return ''

        '''
            «FOR unknownSchemaNode : unknownSchemaNodes»
                «writeUnknownSchemaNode(unknownSchemaNode)»
            «ENDFOR»
        '''
    }

    def private static writeUnknownSchemaNode(UnknownSchemaNode unknownSchemaNode) {
        return ''
    }

    def private static writeUsesNodes(Set<UsesNode> usesNodes) {
        if (usesNodes === null) {
            return ''
        }

        '''
            «FOR usesNode : usesNodes»
                «IF usesNode !== null»
                «writeUsesNode(usesNode)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeUsesNode(UsesNode usesNode) {
        val hasRefines = !usesNode.refines.empty

        '''
            uses «usesNode.groupingPath.pathFromRoot.head.localName»«IF !hasRefines»;«ELSE» {«ENDIF»
            «IF hasRefines»
                «writeRefines(usesNode.refines)»
            }
            «ENDIF»
        '''
    }

    def private static writeRefines(Map<SchemaPath, SchemaNode> refines) {
        '''
            «FOR path : refines.keySet»
            «val schemaNode = refines.get(path)»
            «writeRefine(path, schemaNode)»
            «ENDFOR»
        '''
    }

    def private static writeRefine(SchemaPath path, SchemaNode schemaNode) {
        '''
            refine «path.pathFromRoot.last» {
                «IF schemaNode instanceof DataSchemaNode»
                «writeDataSchemaNode(schemaNode)»
                «ENDIF»
            }
        '''
    }

    def private static writeTypeDefinition(TypeDefinition<?> typeDefinition) {
        var boolean isStatusDeprecated = typeDefinition.status == Status::DEPRECATED
        '''
            type «typeDefinition.QName.localName»«IF !isStatusDeprecated»;«ELSE» {
                status «typeDefinition.status»;
            }
            «ENDIF»
        '''
    }

    def private static writeIdentities(Set<IdentitySchemaNode> identities) {
        if (identities.nullOrEmpty)
            return ''
        '''
            «FOR identity : identities»
                «writeIdentity(identity)»
            «ENDFOR»
        '''
    }

    def private static writeIdentity(IdentitySchemaNode identity) {
        if (identity === null)
            return ''
        '''
            identity «identity.QName.localName» {
                «FOR baseIdentity : identity.baseIdentities»
                base "()«baseIdentity»";
                «ENDFOR»
                «IF identity.description.present»
                description
                    "«identity.description.get»";
                «ENDIF»
                «IF identity.reference.present»
                reference
                    "«identity.reference.get»";
                «ENDIF»
                «IF identity.status !== null»
                status «identity.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeFeatures(Set<FeatureDefinition> features) {
        '''
            «FOR feature : features»
                «IF feature !== null»
                «writeFeature(feature)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeFeature(FeatureDefinition featureDef) {
        '''
            feature «featureDef.QName.localName» {
                «IF featureDef.description.present»
                description
                    "«featureDef.description.get»";
                «ENDIF»
                «IF featureDef.reference.present»
                reference
                    "«featureDef.reference.get»";
                «ENDIF»
                «IF featureDef.status !== null»
                status «featureDef.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeExtensions(List<ExtensionDefinition> extensions) {
        '''
            «FOR anExtension : extensions»
                «IF anExtension !== null»
                «writeExtension(anExtension)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeExtension(ExtensionDefinition extensionDef) {
        '''
            extension «extensionDef.QName.localName» {
                «IF extensionDef.description.present»
                description
                    "«extensionDef.description.get»";
                «ENDIF»
                «IF !extensionDef.argument.nullOrEmpty»
                argument "«extensionDef.argument»";
                «ENDIF»
                «IF extensionDef.reference.present»
                reference
                    "«extensionDef.reference.get»";
                «ENDIF»
                «IF extensionDef.status !== null»
                status «extensionDef.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeDeviations(Set<Deviation> deviations) {
        '''
            «FOR deviation : deviations»
                «IF deviation !== null»
                «writeDeviation(deviation)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeDeviation(Deviation deviation) {
        '''
            deviation «deviation.targetPath» {
                «IF deviation.reference.present»
                    reference
                        "«deviation.reference.get»";
                «ENDIF»
                «FOR dev : deviation.deviates»
                    «IF dev !== null && dev.deviateType !== null»
                        deviation «dev.deviateType.name»;
                    «ENDIF»
                «ENDFOR»
            }
        '''
    }

    def private static writeAugments(Set<AugmentationSchemaNode> augments) {
        '''
            «FOR augment : augments»
                «IF augment !== null»
                «writeAugment(augment)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeDataSchemaNodes(Collection<DataSchemaNode> dataSchemaNodes) {
        '''
            «FOR schemaNode : dataSchemaNodes»
                «writeDataSchemaNode(schemaNode)»
            «ENDFOR»
        '''
    }

    def private static CharSequence writeGroupingDefs(Set<GroupingDefinition> groupingDefs) {
        '''
            «FOR groupingDef : groupingDefs»
                «IF groupingDef !== null»
                «writeGroupingDef(groupingDef)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def private static writeAugment(AugmentationSchemaNode augment) {
        '''
            augment «FormattingUtils.formatToAugmentPath(augment.targetPath.pathFromRoot)» {
                «IF augment.whenCondition !== null && !augment.whenCondition.toString.nullOrEmpty»
                when "«augment.whenCondition.toString»";
                «ENDIF»
                «IF augment.description.present»
                description
                    "«augment.description.get»";
                «ENDIF»
                «IF augment.reference.present»
                reference
                    "«augment.reference.get»";
                «ENDIF»
                «IF augment.status !== null»
                status «augment.status»;
                «ENDIF»
                «IF !augment.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(augment.childNodes)»
                «ENDIF»
                «IF !augment.uses.nullOrEmpty»
                «writeUsesNodes(augment.uses)»
                «ENDIF»
            }
        '''
    }

    def private static writeGroupingDef(GroupingDefinition groupingDef) {
        var boolean isStatusDeprecated = groupingDef.status == Status::DEPRECATED
        '''
            grouping «groupingDef.QName.localName» {
                «IF !groupingDef.groupings.nullOrEmpty»
                    «writeGroupingDefs(groupingDef.groupings)»
                «ENDIF»
                «IF !groupingDef.childNodes.nullOrEmpty»
                    «writeDataSchemaNodes(groupingDef.childNodes)»
                «ENDIF»
                «IF isStatusDeprecated»
                    status «groupingDef.status»;
                «ENDIF»
                «IF !groupingDef.unknownSchemaNodes.nullOrEmpty»
                    «writeUnknownSchemaNodes(groupingDef.unknownSchemaNodes)»
                «ENDIF»
            }
        '''
    }

    def private static writeContSchemaNode(ContainerSchemaNode contSchemaNode) {
        var boolean isStatusDeprecated = contSchemaNode.status == Status::DEPRECATED
        '''
            container «contSchemaNode.getQName.localName» {
                «IF !contSchemaNode.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(contSchemaNode.childNodes)»
                «ENDIF»
                «IF !contSchemaNode.availableAugmentations.nullOrEmpty»
                «writeAugments(contSchemaNode.availableAugmentations)»
                «ENDIF»
                «IF !contSchemaNode.groupings.nullOrEmpty»
                «writeGroupingDefs(contSchemaNode.groupings)»
                «ENDIF»
                «IF !contSchemaNode.uses.nullOrEmpty»
                «writeUsesNodes(contSchemaNode.uses)»
                «ENDIF»
                «IF isStatusDeprecated»
                status «contSchemaNode.status»;
                «ENDIF»
                «IF !contSchemaNode.unknownSchemaNodes.nullOrEmpty»
                «writeUnknownSchemaNodes(contSchemaNode.unknownSchemaNodes)»
                «ENDIF»
            }
        '''
    }

    def private static writeAnyXmlSchemaNode(AnyXmlSchemaNode anyXmlSchemaNode) {
        var boolean isStatusDeprecated = anyXmlSchemaNode.status == Status::DEPRECATED
        '''
            anyxml «anyXmlSchemaNode.getQName.localName»«IF !isStatusDeprecated»;«ELSE» {
                status «anyXmlSchemaNode.status»;
            }
            «ENDIF»
        '''
    }

    def private static writeLeafSchemaNode(LeafSchemaNode leafSchemaNode) {
        var boolean isStatusDeprecated = leafSchemaNode.status == Status::DEPRECATED
        '''
            leaf «leafSchemaNode.getQName.localName» {
                type «leafSchemaNode.type.getQName.localName»;
                «IF isStatusDeprecated»
                    status «leafSchemaNode.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeLeafListSchemaNode(LeafListSchemaNode leafListSchemaNode) {
        var boolean isStatusDeprecated = leafListSchemaNode.status == Status::DEPRECATED
        '''
            leaf-list «leafListSchemaNode.getQName.localName» {
                type «leafListSchemaNode.type.getQName.localName»;
                «IF isStatusDeprecated»
                    status «leafListSchemaNode.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeCaseSchemaNode(CaseSchemaNode choiceCaseNode) {
        var boolean isStatusDeprecated = choiceCaseNode.status == Status::DEPRECATED
        '''
            case «choiceCaseNode.getQName.localName» {
                «FOR childNode : choiceCaseNode.childNodes»
                    «writeDataSchemaNode(childNode)»
                «ENDFOR»
                «IF isStatusDeprecated»
                    status «choiceCaseNode.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeChoiceNode(ChoiceSchemaNode choiceNode) {
        var boolean isStatusDeprecated = choiceNode.status == Status::DEPRECATED
        '''
            choice «choiceNode.getQName.localName» {
                «FOR child : choiceNode.cases.values»
                    «writeDataSchemaNode(child)»
                «ENDFOR»
                «IF isStatusDeprecated»
                    status «choiceNode.status»;
                «ENDIF»
            }
        '''
    }

    def private static writeListSchemaNode(ListSchemaNode listSchemaNode) {
        var boolean isStatusDeprecated = listSchemaNode.status == Status::DEPRECATED

        '''
            list «listSchemaNode.getQName.localName» {
                «IF !listSchemaNode.keyDefinition.empty»
                    key «FOR listKey : listSchemaNode.keyDefinition SEPARATOR " "»"«listKey.localName»"«ENDFOR»;
                «ENDIF»
                «IF !listSchemaNode.childNodes.nullOrEmpty»
                    «writeDataSchemaNodes(listSchemaNode.childNodes)»
                «ENDIF»
                «IF !listSchemaNode.availableAugmentations.nullOrEmpty»
                    «writeAugments(listSchemaNode.availableAugmentations)»
                «ENDIF»
                «IF !listSchemaNode.groupings.nullOrEmpty»
                    «writeGroupingDefs(listSchemaNode.groupings)»
                «ENDIF»
                «IF !listSchemaNode.uses.nullOrEmpty»
                    «writeUsesNodes(listSchemaNode.uses)»
                «ENDIF»
                «IF isStatusDeprecated»
                    status «listSchemaNode.status»;
                «ENDIF»
                «IF !listSchemaNode.unknownSchemaNodes.nullOrEmpty»
                    «writeUnknownSchemaNodes(listSchemaNode.unknownSchemaNodes)»
                «ENDIF»
            }
        '''
    }

    def private static CharSequence writeDataSchemaNode(DataSchemaNode child) {
        '''
            «IF child instanceof ContainerSchemaNode»
                «writeContSchemaNode(child)»
            «ENDIF»
            «IF child instanceof AnyXmlSchemaNode»
                «writeAnyXmlSchemaNode(child)»
            «ENDIF»
            «IF child instanceof LeafSchemaNode»
                «writeLeafSchemaNode(child)»
            «ENDIF»
            «IF child instanceof LeafListSchemaNode»
                «writeLeafListSchemaNode(child)»
            «ENDIF»
            «IF child instanceof CaseSchemaNode»
                «writeCaseSchemaNode(child)»
            «ENDIF»
            «IF child instanceof ChoiceSchemaNode»
                «writeChoiceNode(child)»
            «ENDIF»
            «IF child instanceof ListSchemaNode»
                «writeListSchemaNode(child)»
            «ENDIF»
        '''
    }
}
