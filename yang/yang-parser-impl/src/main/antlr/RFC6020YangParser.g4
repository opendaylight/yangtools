//
// Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
parser grammar RFC6020YangParser;

@header {
package antlr4.generated.grammar;
} 

options{
    tokenVocab=RFC6020YangLexer;
    
}

yang : module_stmt | submodule_stmt ;

module_stmt : MODULE_KEYWORD SEP string 
			  LEFT_BRACE stmtsep 
			      module_header_stmts
			      linkage_stmts 
			      meta_stmts 
			      revision_stmts 
			      body_stmts 
			  RIGHT_BRACE;

submodule_stmt : SUBMODULE_KEYWORD SEP string 
                 LEFT_BRACE stmtsep 
                     submodule_header_stmts 
                     linkage_stmts meta_stmts 
                     revision_stmts 
                     body_stmts 
                 RIGHT_BRACE;
                 
module_header_stmts :  (
						    yang_version_stmt stmtsep 
	                      | namespace_stmt stmtsep 
	                      | prefix_stmt stmtsep 
	                   )+ ;                 

submodule_header_stmts : (
					  		  yang_version_stmt stmtsep 
							| belongs_to_stmt stmtsep
						 )+ ;

meta_stmts : (
				  organization_stmt stmtsep 
				| contact_stmt stmtsep
				| description_stmt stmtsep 
				| reference_stmt stmtsep
			)*;
			
linkage_stmts : (
				  import_stmt stmtsep 
				| include_stmt stmtsep
				)*;			

revision_stmts :  (revision_stmt stmtsep)*;

body_stmts :   (( extension_stmt 
				| feature_stmt 
				| identity_stmt 
				| typedef_stmt 
				| grouping_stmt 
				| data_def_stmt 
//				| augment_stmt 
//				| rpc_stmt 
//				| notification_stmt 
//				| deviation_stmt
				) 
				stmtsep)*;

data_def_stmt :   container_stmt 
//                | leaf_stmt 
//                | leaf_list_stmt 
//                | list_stmt 
//                | choice_stmt 
//                | anyxml_stmt 
//                | uses_stmt
                ;

yang_version_stmt : YANG_VERSION_KEYWORD SEP string stmtend;

import_stmt : IMPORT_KEYWORD SEP string 
			  LEFT_BRACE stmtsep 
			      prefix_stmt stmtsep  
			      (revision_date_stmt stmtsep)?
			  RIGHT_BRACE;

include_stmt : INCLUDE_KEYWORD SEP string 
               (SEMICOLON | 
               	LEFT_BRACE stmtsep  
               		(revision_date_stmt stmtsep)? 
               	RIGHT_BRACE);

namespace_stmt : NAMESPACE_KEYWORD SEP string stmtend;

prefix_stmt : PREFIX_KEYWORD SEP string stmtend;

belongs_to_stmt : BELONGS_TO_KEYWORD SEP string 
				  LEFT_BRACE stmtsep 
				      prefix_stmt stmtsep  
				  RIGHT_BRACE;

organization_stmt : ORGANIZATION_KEYWORD SEP string stmtend;

contact_stmt : CONTACT_KEYWORD SEP string stmtend;

description_stmt : DESCRIPTION_KEYWORD SEP string stmtend;

reference_stmt : REFERENCE_KEYWORD SEP string stmtend;

units_stmt : UNITS_KEYWORD SEP string stmtend;

revision_stmt : REVISION_KEYWORD SEP string 
				(SEMICOLON | 
			     LEFT_BRACE stmtsep  
			         (description_stmt stmtsep)? 
			         (reference_stmt stmtsep)? 
			     RIGHT_BRACE);
				
revision_date_stmt : REVISION_DATE_KEYWORD SEP string stmtend;

extension_stmt : EXTENSION_KEYWORD SEP string 
				 (SEMICOLON | 
					LEFT_BRACE stmtsep  
					    (   argument_stmt stmtsep 
					      | status_stmt stmtsep
					      | description_stmt stmtsep 
					      | reference_stmt stmtsep
					    )* 
					RIGHT_BRACE
			  	 );

argument_stmt : ARGUMENT_KEYWORD SEP string 
				(SEMICOLON | 
				 LEFT_BRACE stmtsep 
				 	 (yin_element_stmt stmtsep)? 
				 RIGHT_BRACE
				);
				
yin_element_stmt : YIN_ELEMENT_KEYWORD SEP yin_element_arg stmtend;		

yin_element_arg : string; // TRUE_KEYWORD | FALSE_KEYWORD;		

identity_stmt : IDENTITY_KEYWORD SEP string 
                (SEMICOLON | 
                 LEFT_BRACE stmtsep 
                     (   base_stmt stmtsep 
                       | status_stmt stmtsep
                       | description_stmt stmtsep
                       | reference_stmt stmtsep
                     )* 
                 RIGHT_BRACE
                );

base_stmt : BASE_KEYWORD SEP string stmtend;

feature_stmt : FEATURE_KEYWORD SEP string 
				(SEMICOLON | 
				 LEFT_BRACE stmtsep  
				    (   if_feature_stmt stmtsep
				      | status_stmt stmtsep
				      | description_stmt stmtsep
				      | reference_stmt stmtsep
				    )* 
				 RIGHT_BRACE  
				);

if_feature_stmt : IF_FEATURE_KEYWORD SEP string stmtend;

typedef_stmt : TYPEDEF_KEYWORD SEP string 
               LEFT_BRACE stmtsep 
                  (   type_stmt stmtsep 
                  	| units_stmt stmtsep
                  	| default_stmt stmtsep
                  	| status_stmt stmtsep
                  	| description_stmt stmtsep
                  	| reference_stmt stmtsep
                  )+ 
               RIGHT_BRACE;

type_stmt : TYPE_KEYWORD SEP string 
            (SEMICOLON | 
             LEFT_BRACE stmtsep 
                 type_body_stmts 
             RIGHT_BRACE   
            );

type_body_stmts :  numerical_restrictions 
                 | decimal64_specification 
                 | string_restrictions 
                 | enum_specification 
                 | leafref_specification 
                 | identityref_specification 
                 | instance_identifier_specification 
                 | bits_specification 
                 | union_specification
                 ;

numerical_restrictions : range_stmt stmtsep;

range_stmt : RANGE_KEYWORD SEP string 
             (SEMICOLON | 
              LEFT_BRACE stmtsep 
                  (  // identifier_stmt stmtsep 
                  	 error_message_stmt stmtsep 
                  	| error_app_tag_stmt stmtsep 
                  	| description_stmt stmtsep 
                  	| reference_stmt stmtsep
                  )* 
              RIGHT_BRACE
             );

decimal64_specification : fraction_digits_stmt;

fraction_digits_stmt : FRACTION_DIGITS_KEYWORD SEP string stmtend;

string_restrictions : (length_stmt | pattern_stmt )*;

length_stmt : LENGTH_KEYWORD SEP string 
              (SEMICOLON | 
               LEFT_BRACE stmtsep 
                  (   error_message_stmt stmtsep
                  	| error_app_tag_stmt stmtsep
                  	| description_stmt stmtsep
                  	| reference_stmt stmtsep
                  )* 
                RIGHT_BRACE 
              );

pattern_stmt : PATTERN_KEYWORD SEP string 
               (SEMICOLON | 
               	LEFT_BRACE stmtsep 
               	    (   error_message_stmt stmtsep
               	      | error_app_tag_stmt stmtsep
               	      | description_stmt stmtsep
               	      | reference_stmt stmtsep
               	    )* 
               	 RIGHT_BRACE
               );
               
default_stmt : DEFAULT_KEYWORD SEP string stmtend;

enum_specification : enum_stmt stmtsep;       

enum_stmt : ENUM_KEYWORD SEP string 
            (SEMICOLON | 
             LEFT_BRACE stmtsep  
                (   value_stmt stmtsep
                  | status_stmt stmtsep
                  | description_stmt stmtsep
                  | reference_stmt stmtsep
                )* 
             RIGHT_BRACE
            );        

leafref_specification : path_stmt stmtsep;

path_stmt : PATH_KEYWORD SEP string stmtend;

require_instance_stmt : REQUIRE_INSTANCE_KEYWORD SEP require_instance_arg stmtend;

require_instance_arg :string; // TRUE_KEYWORD | FALSE_KEYWORD;

instance_identifier_specification : (require_instance_stmt stmtsep)?;

identityref_specification : base_stmt stmtsep;

union_specification : type_stmt stmtsep;

bits_specification : bit_stmt stmtsep;

bit_stmt : BIT_KEYWORD SEP string 
           (SEMICOLON | 
            LEFT_BRACE  stmtsep 
                 (   position_stmt stmtsep 
                   | status_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                 )* 
            RIGHT_BRACE
           );

position_stmt : POSITION_KEYWORD SEP string stmtend;

status_stmt : STATUS_KEYWORD SEP status_arg stmtend;

status_arg : string; /*CURRENT_KEYWORD | OBSOLETE_KEYWORD | DEPRECATED_KEYWORD; */

config_stmt : CONFIG_KEYWORD SEP config_arg stmtend;

config_arg : string; //  TRUE_KEYWORD | FALSE_KEYWORD;

mandatory_stmt : MANDATORY_KEYWORD SEP mandatory_arg stmtend;

mandatory_arg :string; // TRUE_KEYWORD | FALSE_KEYWORD;

presence_stmt : PRESENCE_KEYWORD SEP string stmtend;

ordered_by_stmt : ORDERED_BY_KEYWORD SEP ordered_by_arg stmtend;

ordered_by_arg : string; /*USER_KEYWORD | SYSTEM_KEYWORD;*/

must_stmt : MUST_KEYWORD SEP string 
            (SEMICOLON | 
             LEFT_BRACE stmtsep  
                (  //  identifier_stmt stmtsep 
                    error_message_stmt stmtsep 
                   | error_app_tag_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                )* 
             RIGHT_BRACE
            );

error_message_stmt : ERROR_MESSAGE_KEYWORD SEP string stmtend;

error_app_tag_stmt : ERROR_APP_TAG_KEYWORD SEP string stmtend;

min_elements_stmt : MIN_ELEMENTS_KEYWORD SEP min_value_arg stmtend;

min_value_arg : /*UNBOUNDED_KEYWORD |*/ string;

max_elements_stmt : MAX_ELEMENTS_KEYWORD SEP max_value_arg stmtend;

max_value_arg : /*UNBOUNDED_KEYWORD |*/ string;

value_stmt : VALUE_KEYWORD SEP string stmtend;

grouping_stmt : GROUPING_KEYWORD SEP string 
                (SEMICOLON | 
                 LEFT_BRACE stmtsep 
                     (   status_stmt stmtsep
                       | description_stmt stmtsep 
                       | reference_stmt  stmtsep
                       | typedef_stmt stmtsep 
                       | grouping_stmt stmtsep 
                       | data_def_stmt stmtsep 
                       
                     )* 
                 RIGHT_BRACE
               );

container_stmt : CONTAINER_KEYWORD SEP string 
				 (SEMICOLON | 
				  LEFT_BRACE  stmtsep 
				       (   
				       	 //when_stmt stmtsep
				       	  if_feature_stmt stmtsep
				       	 | must_stmt stmtsep
				       	 | presence_stmt stmtsep
				       	 | config_stmt stmtsep
				       	 | status_stmt stmtsep
				       	 | description_stmt stmtsep
				       	 | reference_stmt stmtsep
				       	 | typedef_stmt stmtsep
				       	 | grouping_stmt stmtsep
				       	 | data_def_stmt stmtsep
				       	 
				       )* 
				   RIGHT_BRACE
				 );

unknown_statement : prefix COLON identifier (SEP string)? 
                    (SEMICOLON | LEFT_BRACE unknown_statement2* RIGHT_BRACE);
                    
unknown_statement2 : (prefix COLON)? identifier (SEP string)? 
                     (SEMICOLON | LEFT_BRACE unknown_statement2* RIGHT_BRACE);

prefix : IDENTIFIER;

identifier: IDENTIFIER;

stmtend : SEMICOLON | LEFT_BRACE unknown_statement* RIGHT_BRACE;

stmtsep : unknown_statement*;

string : STRING (PLUS STRING)*;

	  
			  
			  
			  
