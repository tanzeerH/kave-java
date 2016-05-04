grammar TypeNaming;

options { language=Java; }

@lexer::header {
/**
 * Copyright 2016 Sebastian Proksch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.commons.model.names.csharp.parser;
}

@parser::header {
/**
 * Copyright 2016 Sebastian Proksch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.commons.model.names.csharp.parser;
}

typeEOL : type EOL;
methodEOL: method EOL;

type: UNKNOWN | typeParameter | regularType | delegateType | arrayType;
typeParameter : id;
regularType: ( resolvedType | nestedType ) ',' WS? assembly;
delegateType: 'd:' method;
arrayType: 'arr(' POSNUM '):' type;

nestedType: 'n:' nestedTypeName '+' typeName;
nestedTypeName: nestedType | resolvedType;


resolvedType: namespace? typeName;
namespace : (id '.')+;
typeName: enumTypeName | possiblyGenericTypeName;
possiblyGenericTypeName: ( interfaceTypeName | structTypeName | simpleTypeName ) genericTypePart?;

enumTypeName: 'e:' simpleTypeName;
interfaceTypeName: 'i:' simpleTypeName;
structTypeName: 's:' simpleTypeName;
simpleTypeName: id;


genericTypePart: '\'' POSNUM '[' genericParam (',' genericParam)* ']';
genericParam: '[' boundTypeParameter ']';
boundTypeParameter: typeParameter (WS? '->' WS? type)?;


assembly: id (',' WS? assemblyVersion)? ;
assemblyVersion: num '.' num '.' num '.' num;	

method: (constructor | customMethod) '(' WS? ( formalParam ( WS? ',' WS? formalParam)*)? WS? ')';
constructor: '[' type '].' constructorName;
constructorName: ('.ctor' | '.cctor');
customMethod: '[' type ']' WS? (staticModifier)? WS? '[' type '].' id genericTypePart?;
formalParam: (WS? parameterModifier)? WS? '[' type ']' WS? id;
parameterModifier: (paramsModifier | optsModifier | refModifier | outModifier | extensionModifier);

staticModifier: 'static';
paramsModifier: 'params';
optsModifier: 'opts';
refModifier: 'ref';
outModifier: 'out';
extensionModifier: 'this';

// basic
UNKNOWN:'?';
id: LETTER (LETTER|num|SIGN)*;
num: '0' | POSNUM;
POSNUM:DIGIT_NON_ZERO DIGIT*;
LETTER:'a'..'z'|'A'..'Z';
SIGN:'+'|'-'|'*'|'/'|'_'|';'|':'|'='|'$'|'#'|'@'|'!';
fragment DIGIT:'0'|DIGIT_NON_ZERO;
fragment DIGIT_NON_ZERO: '1'..'9';
//WS: (' ' | '\t') -> skip;
WS: (' '| '\t')+;
EOL:'\n';
