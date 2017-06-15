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
import java.util.Date;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;

interface YangModuleWriter {

    void endNode();

    void startModuleNode(String identifier);

    void startOrganizationNode(String input);

    void startContactNode(String input);

    void startDescriptionNode(String input);

    void startUnitsNode(String input);

    void startYangVersionNode(String input);

    void startNamespaceNode(URI uri);

    void startKeyNode(List<QName> keyList);

    void startPrefixNode(String input);

    void startFeatureNode(QName qName);

    void startExtensionNode(QName qName);

    void startArgumentNode(String input);

    void startStatusNode(Status status);

    void startTypeNode(QName qName);

    void startLeafNode(QName qName);

    void startContainerNode(QName qName);

    void startGroupingNode(QName qName);

    void startRpcNode(QName qName);

    void startInputNode();

    void startOutputNode();

    void startLeafListNode(QName qName);

    void startListNode(QName qName);

    void startChoiceNode(QName qName);

    void startCaseNode(QName qName);

    void startNotificationNode(QName qName);

    void startIdentityNode(QName qName);

    void startBaseNode(QName qName);

    void startTypedefNode(QName qName);

    void startRevisionNode(Date date);

    void startDefaultNode(String string);

    void startMustNode(RevisionAwareXPath xpath);

    void startErrorMessageNode(String input);

    void startErrorAppTagNode(String input);

    void startPatternNode(String regularExpression);

    void startValueNode(Integer integer);

    void startEnumNode(String name);

    void startRequireInstanceNode(boolean require);

    void startPathNode(RevisionAwareXPath revisionAwareXPath);

    void startBitNode(String name);

    void startPositionNode(UnsignedInteger position);

    void startReferenceNode(String input);

    void startRevisionDateNode(Date date);

    void startImportNode(String moduleName);

    void startUsesNode(QName groupingName);

    void startAugmentNode(SchemaPath targetPath);

    void startConfigNode(boolean config);

    void startLengthNode(String lengthString);

    void startMaxElementsNode(Integer max);

    void startMinElementsNode(Integer min);

    void startPresenceNode(boolean presence);

    void startOrderedByNode(String ordering);

    void startRangeNode(String rangeString);

    void startRefineNode(SchemaPath path);

    void startMandatoryNode(boolean mandatory);

    void startAnyxmlNode(QName qName);

    void startUnknownNode(StatementDefinition def);

    void startUnknownNode(StatementDefinition def, String nodeParameter);

    void startFractionDigitsNode(Integer fractionDigits);

    void startYinElementNode(boolean yinElement);

    void startWhenNode(RevisionAwareXPath revisionAwareXPath);

    void startAnydataNode(QName qName);

    void startActionNode(QName qName);

    void startModifierNode(ModifierKind modifier);

    void startUniqueNode(UniqueConstraint uniqueConstraint);

    void startRevisionNode(String rawArgument);

    void startRevisionDateNode(String rawArgument);

    void startExtensionNode(String rawArgument);

    void startBaseNode(String rawArgument);

    void startFeatureNode(String rawArgument);

    void startYinElementNode(String rawArgument);

    void startIdentityNode(String rawArgument);

    void startTypedefNode(String rawArgument);

    void startRpcNode(String rawArgument);

    void startTypeNode(String rawArgument);

    void startContainerNode(String rawArgument);

    void startPresenceNode(String rawArgument);

    void startStatusNode(String rawArgument);

    void startConfigNode(String rawArgument);

    void startLeafNode(String rawArgument);

    void startWhenNode(String rawArgument);

    void startLeafListNode(String rawArgument);

    void startMustNode(String rawArgument);

    void startMinElementsNode(String rawArgument);

    void startMaxElementsNode(String rawArgument);

    void startListNode(String rawArgument);

    void startKeyNode(String rawArgument);

    void startUniqueNode(String rawArgument);

    void startActionNode(String rawArgument);

    void startChoiceNode(String rawArgument);

    void startMandatoryNode(String rawArgument);

    void startAnyxmlNode(String rawArgument);

    void startCaseNode(String rawArgument);

    void startAnydataNode(String rawArgument);

    void startGroupingNode(String rawArgument);

    void startUsesNode(String rawArgument);

    void startRefineNode(String rawArgument);

    void startAugmentNode(String rawArgument);

    void startNotificationNode(String rawArgument);

    void startValueNode(String rawArgument);

    void startModifierNode(String rawArgument);

    void startFractionDigitsNode(String rawArgument);

    void startPathNode(String rawArgument);

    void startRequireInstanceNode(String rawArgument);

    void startPositionNode(String rawArgument);
}
