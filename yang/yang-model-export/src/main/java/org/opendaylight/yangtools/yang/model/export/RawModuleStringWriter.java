/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

interface RawModuleStringWriter {

    void endNode();

    void startModuleNode(String identifier);

    void startOrganizationNode(String input);

    void startContactNode(String input);

    void startDescriptionNode(String input);

    void startUnitsNode(String input);

    void startYangVersionNode(String input);

    void startNamespaceNode(String uri);

    void startKeyNode(String keyList);

    void startPrefixNode(String input);

    void startFeatureNode(String String);

    void startExtensionNode(String String);

    void startArgumentNode(String input);

    void startStatusNode(String status);

    void startTypeNode(String String);

    void startLeafNode(String String);

    void startContainerNode(String String);

    void startGroupingNode(String String);

    void startRpcNode(String String);

    void startInputNode();

    void startOutputNode();

    void startLeafListNode(String String);

    void startListNode(String String);

    void startChoiceNode(String String);

    void startCaseNode(String String);

    void startNotificationNode(String String);

    void startIdentityNode(String String);

    void startBaseNode(String String);

    void startTypedefNode(String String);

    void startRevisionNode(String date);

    void startDefaultNode(String string);

    void startMustNode(String xpath);

    void startErrorMessageNode(String input);

    void startErrorAppTagNode(String input);

    void startPatternNode(String regularExpression);

    void startValueNode(String integer);

    void startEnumNode(String name);

    void startRequireInstanceNode(String require);

    void startPathNode(String revisionAwareXPath);

    void startBitNode(String name);

    void startPositionNode(String position);

    void startReferenceNode(String input);

    void startRevisionDateNode(String date);

    void startImportNode(String moduleName);

    void startUsesNode(String groupingName);

    void startAugmentNode(String targetPath);

    void startConfigNode(String config);

    void startLengthNode(String lengthString);

    void startMaxElementsNode(String max);

    void startMinElementsNode(String min);

    void startPresenceNode(String presence);

    void startOrderedByNode(String ordering);

    void startRangeNode(String rangeString);

    void startRefineNode(String path);

    void startMandatoryNode(String mandatory);

    void startAnyxmlNode(String String);

    void startUnknownNode(String nodeParameter);

    void startFractionDigitsNode(String fractionDigits);

    void startYinElementNode(String yinElement);

    void startWhenNode(String revisionAwareXPath);

    void startAnydataNode(String String);

    void startActionNode(String String);

    void startModifierNode(String modifier);

    void startUniqueNode(String uniqueConstraint);

}
