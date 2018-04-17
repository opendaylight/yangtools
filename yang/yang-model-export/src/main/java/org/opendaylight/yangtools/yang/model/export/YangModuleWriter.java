/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.primitives.UnsignedInteger;
import java.net.URI;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;

@Deprecated
interface YangModuleWriter {
    void endNode();

    void startActionNode(QName qname);

    void startActionNode(String rawArgument);

    void startAnydataNode(QName qname);

    void startAnydataNode(String rawArgument);

    void startAnyxmlNode(QName qname);

    void startAnyxmlNode(String rawArgument);

    void startArgumentNode(String input);

    void startAugmentNode(SchemaPath targetPath);

    void startAugmentNode(String rawArgument);

    void startBaseNode(QName qname);

    void startBaseNode(String rawArgument);

    void startBelongsToNode(String rawArgument);

    void startBitNode(String name);

    void startCaseNode(QName qname);

    void startCaseNode(String rawArgument);

    void startChoiceNode(QName qname);

    void startChoiceNode(String rawArgument);

    void startConfigNode(boolean config);

    void startConfigNode(String rawArgument);

    void startContactNode(String input);

    void startContainerNode(QName qname);

    void startContainerNode(String rawArgument);

    void startDefaultNode(String string);

    void startDescriptionNode(String input);

    void startDeviateNode(String rawArgument);

    void startDeviationNode(String rawArgument);

    void startEnumNode(String name);

    void startErrorAppTagNode(String input);

    void startErrorMessageNode(String input);

    void startExtensionNode(QName qname);

    void startExtensionNode(String rawArgument);

    void startFeatureNode(QName qname);

    void startFeatureNode(String rawArgument);

    void startFractionDigitsNode(Integer fractionDigits);

    void startFractionDigitsNode(String rawArgument);

    void startGroupingNode(QName qname);

    void startGroupingNode(String rawArgument);

    void startIdentityNode(QName qname);

    void startIdentityNode(String rawArgument);

    void startIfFeatureNode(String rawArgument);

    void startImportNode(String moduleName);

    void startIncludeNode(String rawArgument);

    void startInputNode();

    void startKeyNode(List<QName> keyList);

    void startKeyNode(String rawArgument);

    void startLeafListNode(QName qname);

    void startLeafListNode(String rawArgument);

    void startLeafNode(QName qname);

    void startLeafNode(String rawArgument);

    void startLengthNode(String lengthString);

    void startListNode(QName qname);

    void startListNode(String rawArgument);

    void startMandatoryNode(boolean mandatory);

    void startMandatoryNode(String rawArgument);

    void startMaxElementsNode(Integer max);

    void startMaxElementsNode(String rawArgument);

    void startMinElementsNode(Integer min);

    void startMinElementsNode(String rawArgument);

    void startModifierNode(ModifierKind modifier);

    void startModifierNode(String rawArgument);

    void startModuleNode(String identifier);

    void startMustNode(RevisionAwareXPath xpath);

    void startMustNode(String rawArgument);

    void startNamespaceNode(URI uri);

    void startNotificationNode(QName qname);

    void startNotificationNode(String rawArgument);

    void startOrderedByNode(String ordering);

    void startOrganizationNode(String input);

    void startOutputNode();

    void startPathNode(RevisionAwareXPath revisionAwareXPath);

    void startPathNode(String rawArgument);

    void startPatternNode(String regularExpression);

    void startPositionNode(String rawArgument);

    void startPositionNode(UnsignedInteger position);

    void startPrefixNode(String input);

    void startPresenceNode(boolean presence);

    void startPresenceNode(String rawArgument);

    void startRangeNode(String rangeString);

    void startReferenceNode(String input);

    void startRefineNode(SchemaPath path);

    void startRefineNode(String rawArgument);

    void startRequireInstanceNode(boolean require);

    void startRequireInstanceNode(String rawArgument);

    void startRevisionDateNode(Revision date);

    void startRevisionDateNode(String rawArgument);

    void startRevisionNode(Revision date);

    void startRevisionNode(String rawArgument);

    void startRpcNode(QName qname);

    void startRpcNode(String rawArgument);

    void startStatusNode(Status status);

    void startStatusNode(String rawArgument);

    void startSubmoduleNode(String rawArgument);

    void startTypedefNode(QName qname);

    void startTypedefNode(String rawArgument);

    void startTypeNode(QName qname);

    void startTypeNode(String rawArgument);

    void startUniqueNode(String rawArgument);

    void startUniqueNode(UniqueConstraint uniqueConstraint);

    void startUnitsNode(String input);

    void startUnknownNode(StatementDefinition def);

    void startUnknownNode(StatementDefinition def, String nodeParameter);

    void startUsesNode(QName groupingName);

    void startUsesNode(String rawArgument);

    void startValueNode(Integer integer);

    void startValueNode(String rawArgument);

    void startWhenNode(RevisionAwareXPath revisionAwareXPath);

    void startWhenNode(String rawArgument);

    void startYangVersionNode(String input);

    void startYinElementNode(boolean yinElement);

    void startYinElementNode(String rawArgument);
}
