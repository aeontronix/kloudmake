grammar SystyrantLang;

// Gramar

start: topLvlFunctions* EOF;

topLvlFunctions: importPkg | includeFile | statement;

statement: ( create=createResource | invoke=invokeMethod | define=resourceDefinition | asvar=assignVariableStatement );

// Imports & Includes

importPkg: IMPORT ( packageName | fullyQualifiedId ) SC;

includeFile: INCLUDE string SC;

// Define Resource

resourceDefinition: DEF fullyQualifiedId resourceDefinitionParams? ( resourceDefinitionStatements | SC );

resourceDefinitionParams: '(' parameterAssignment* ')';

resourceDefinitionStatements: LCB statement* RCB;

// Assign variable

assignVariableStatement: DOLLAR var=anyId EQ val=staticOrDynamicValue SC;

// Create Resource

createResource: ldep=createResource ( rlk=ARWR | llk=ARWL ) rdep=createResource | NEW elname=fullyQualifiedId params=createResourceParams? ( createResourceStatements | SC );

createResourceDepLinking: ARWR | ARWL;

createResourceStatements: '{' ( createResourceSingleInstance | createResourceMultipleInstance* ) '}';

createResourceParams: '(' parameterAssignment* ')';

createResourceSingleInstance: createResourceInstanceId? createResourceInstanceElements*;

createResourceMultipleInstance: createResourceInstanceId? createResourceInstanceElements* SC;

createResourceInstanceId: id=string COL ?;

createResourceInstanceElements: asvar=assignVariableStatement | aspar=parameterAssignment | child=createResourceInstanceChild;

createResourceInstanceChild: createResource COMMA?;

// Invoke Method

invokeMethod: methodName=anyId '(' parameter* ')';

// Query language

query : queryExpression EOF;

queryExpression : '(' bracketExpr=queryExpression ')' | queryExpression bOp=binaryOp queryExpression | attrMatch=queryAttrMatch | co=queryChildOfMatch | tm=queryTypeMatch | uid=queryUidMatch | id=queryIdMatch | sh=querySameHostMatch;

queryAttrMatch : AT attr=anyId ( nnul=queryAttrMatchNonNull | nul=queryAttrMatchNullOrEmpty );

queryUidMatch : anyId DOT anyId ( DOT anyId )*;

queryIdMatch : staticOrDynamicValue;

queryAttrMatchNonNull : n=NOT? op=queryAttrMatchOp val=string;

queryAttrMatchNullOrEmpty : IS n=NOT? (nul=NULL | empty=EMPTY);

queryAttrMatchOp : eq=(FEQ |EQS) | lk=( EQ | LIKE ) | rgx=( REGEX | TEQ ) | m=( GT | GTS ) | l=(LT | LTS);

queryChildOfMatch : CHILDOF s=STAR? exp=queryExpression?;

queryDependsMatch : DEPENDS s=STAR? exp=queryExpression?;

querySameHostMatch : SAMEHOST;

queryTypeMatch : TYPE t=fullyQualifiedIdWithPkg;

// Requires support

requires : r1=requiresType* ( COMMA r2=requiresType )* EOF;

requiresType : id=fullyQualifiedId attrs=requiresTypeAttrs?;

requiresTypeAttrs : LB attr=requiresTypeAttrsMatch RB ;

requiresTypeAttrsMatch : a=parameterAssignment*;

// Common

logOp : EQ | NEQ;

binaryOp : a=AND | o=OR;

parameter: ( id=anyId EQ )? staticOrDynamicValue COMMA?;

parameterAssignment: paramName=anyId EQ staticOrDynamicValue COMMA?;

staticValue: qstr=QSTRING | uqstr=UQSTRING | nb=NB | id=anyId;

staticOrDynamicValue: st=staticValue | dyn=dynamicValue | iv=invokeMethod;

dynamicValue: ASTRING | variableLookupValue;

variableLookupValue: DOLLAR anyId;

packageName: anyId ( DOT anyId )*;

fullyQualifiedId: ( packageName ':' )? anyId ;

fullyQualifiedIdWithPkg: packageName ':' anyId ;

anyId: ID | IMPORT | INCLUDE | IMPORT | NEW | AND | OR | NOT | EQS | LIKE | REGEX | IS | NULL | EMPTY | CHILDOF | TYPE | DEPENDS ;

string: astr=ASTRING | sval=staticValue;

// Lexer

DOLLAR: '$';
AND: 'and';
OR: 'or';
NOT: 'not';
REGEX: 'regex';
TYPE: 'type';
EMPTY: 'empty';
SAMEHOST: 'samehost';
CHILDOF: 'childof';
DEPENDS: 'depends';
IS: 'is';
NULL: 'null';
EQS: 'eq';
LIKE: 'like';
EQ: '=';
FEQ: '==';
TEQ: '~=';
ARWR: '->';
ARWL: '<-';
GT: '>';
GTS: 'gt';
LT: '<';
LTS: 'lt';
NEQ: '!=';
STAR: '*';
AT: '@';
LCB: '{';
RCB: '}';
LB: '(';
RB: ')';
LSB: '[';
RSB: ']';
COMMA: ',';
COL: ':';
DOT: '.';

INCLUDE : 'include';
DEF : 'def';
NEW : 'new';
SC : ';';
IMPORT : 'import';
LF: '\n' -> skip;
WS: [ \r\t]+ -> skip;
NB: ([0-9] | '.')+;
ID: [a-zA-Z] [a-zA-Z0-9-_]*;
UQSTRING: [a-zA-Z0-9-_/$%^&*!];
QSTRING: '\'' ( ESCAPE | ~('\''|'\\') )* '\'';
ASTRING: '"' ( ESCAPE | ~('"'|'\\') )* '"';
fragment
ESCAPE : '\\' . ;