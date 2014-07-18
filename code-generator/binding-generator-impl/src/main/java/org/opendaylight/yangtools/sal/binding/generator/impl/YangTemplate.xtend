/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl

import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import org.opendaylight.yangtools.yang.model.api.ChoiceNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import java.util.Set
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.ModuleImport
import java.util.Date
import java.text.SimpleDateFormat
import java.util.StringTokenizer

class YangTemplate {

    def static String generateYangSnipet(DataSchemaNode schemaNode) {
        '''
            «writeDataSchemaNode(schemaNode)»
        '''
    }
    
    def static String writeModuleImports(Set<ModuleImport> moduleImports) {
        '''
        «FOR moduleImport : moduleImports SEPARATOR "\n"»
        import «moduleImport.moduleName» { prefix "«moduleImport.prefix»"; }
        «ENDFOR»
        '''
    }
    
    def static formatDate(Date moduleRevision) {
        val SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd")
        return dateFormat.format(moduleRevision)
    }
    
    def static writeRevision(Date moduleRevision, String moduleDescription) {
        val revisionIndent = 12
        '''
        revision «formatDate(moduleRevision)» {
            description "«formatToParagraph(moduleDescription, revisionIndent)»";
        }
        '''
    }
    
    def static String generateYangSnipet(Module module) {
        val schemaNodes = module.childNodes
        
        '''
        module «module.name» {
            yang-version «module.yangVersion»;
            namespace "«module.QNameModule.namespace.toString»";
            prefix "«module.prefix»";
            
            «writeModuleImports(module.imports)»
            «writeRevision(module.revision, module.description)»
            
            «FOR schemaNode : schemaNodes»
                «writeDataSchemaNode(schemaNode)»
            «ENDFOR»
        }
        '''
    }

    def static writeContSchemaNode(ContainerSchemaNode contSchemaNode) {
        '''
            container «contSchemaNode.getQName.localName» {
                «FOR child : contSchemaNode.childNodes»
                    «writeDataSchemaNode(child)»
                «ENDFOR»
            }
        '''
    }

    def static writeAnyXmlSchemaNode(AnyXmlSchemaNode anyXmlSchemaNode) {
        '''
            anyxml «anyXmlSchemaNode.getQName.localName»;
        '''
    }

    def static writeLeafSchemaNode(LeafSchemaNode leafSchemaNode) {
        '''
            leaf «leafSchemaNode.getQName.localName» {
                type «leafSchemaNode.type.getQName.localName»;
            }
        '''
    }

    def static writeLeafListSchemaNode(LeafListSchemaNode leafListSchemaNode) {
        '''
            leaf-list «leafListSchemaNode.getQName.localName» {
                type «leafListSchemaNode.type.getQName.localName»;
            }
        '''
    }

    def static writeChoiceCaseNode(ChoiceCaseNode choiceCaseNode) {
        '''
            case «choiceCaseNode.getQName.localName» {
                «FOR childNode : choiceCaseNode.childNodes»
                «writeDataSchemaNode(childNode)»
                «ENDFOR»
            }
        '''
    }

    def static writeChoiceNode(ChoiceNode choiceNode) {
        '''
            choice «choiceNode.getQName.localName» {
                «FOR child : choiceNode.cases»
                    «writeDataSchemaNode(child)»
                «ENDFOR»
            }
        '''
    }

    def static writeListSchemaNode(ListSchemaNode listSchemaNode) {
        '''
            list «listSchemaNode.getQName.localName» {
                key «FOR listKey : listSchemaNode.keyDefinition SEPARATOR " "»"«listKey.localName»"
                «ENDFOR»
                «FOR child : listSchemaNode.childNodes»
                    «writeDataSchemaNode(child)»
                «ENDFOR»
            }
        '''
    }

    def static writeDataSchemaNode(DataSchemaNode child) {
        '''
        «IF child instanceof ContainerSchemaNode»
            «writeContSchemaNode(child as ContainerSchemaNode)»
        «ENDIF» 
        «IF child instanceof AnyXmlSchemaNode»
            «writeAnyXmlSchemaNode(child as AnyXmlSchemaNode)»
        «ENDIF»
        «IF child instanceof LeafSchemaNode»
            «writeLeafSchemaNode(child as LeafSchemaNode)»
        «ENDIF»
        «IF child instanceof LeafListSchemaNode»
            «writeLeafListSchemaNode(child as LeafListSchemaNode)»
        «ENDIF»
        «IF child instanceof ChoiceCaseNode»
            «writeChoiceCaseNode(child as ChoiceCaseNode)»
        «ENDIF»
        «IF child instanceof ChoiceNode»
            «writeChoiceNode(child as ChoiceNode)»
        «ENDIF»
        «IF child instanceof ListSchemaNode»
            «writeListSchemaNode(child as ListSchemaNode)»
        «ENDIF»
        '''
    }
    
    static def String formatToParagraph(String text, int nextLineIndent) {
        if(text == null || text.isEmpty())
                return text;
        
        var String formattedText = text;
        val StringBuilder sb = new StringBuilder();
        val StringBuilder lineBuilder = new StringBuilder();
        var boolean isFirstElementOnNewLineEmptyChar = false;
        val lineIndent = computeNextLineIndent(nextLineIndent);
            
        formattedText = formattedText.replace("*/", "&#42;&#47;");
        formattedText = formattedText.replace("\n", "");     
        formattedText = formattedText.replace("\t", "");
        formattedText = formattedText.replaceAll(" +", " ");

        val StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);
            
        while(tokenizer.hasMoreElements()) {
            val String nextElement = tokenizer.nextElement().toString();
                    
            if(lineBuilder.length() + nextElement.length() > 80) {
                if (lineBuilder.charAt(lineBuilder.length() - 1) == ' ') {
                    lineBuilder.setLength(0);
                    lineBuilder.append(lineBuilder.substring(0, lineBuilder.length() - 1));
                }
                if (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.setLength(0);
                    lineBuilder.append(lineBuilder.substring(1));
                }
                       
                sb.append(lineBuilder);
                lineBuilder.setLength(0);
                sb.append("\n");
                
                if(nextLineIndent > 0) {
                    sb.append(lineIndent)
                }
                        
                if(nextElement.toString().equals(" "))
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            }
            if(isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            }
            else {
                lineBuilder.append(nextElement);
            }
        }
        sb.append(lineBuilder);
        sb.append("\n");
            
        return sb.toString();
    }
    
    private static def computeNextLineIndent(int nextLineIndent) {
        val StringBuilder sb = new StringBuilder()
        var i = 0
        while(i < nextLineIndent) {
            sb.append(' ')
            i = i+1
        }
        return sb.toString
    } 
}
