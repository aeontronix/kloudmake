grammar Test;

start: createResource;

createResource: fullyQualifiedId '{' ( createResourceSingleInstance | createResourceMultipleInstance* ) '}';

createResourceSingleInstance: createResourceInstanceId? createResourceInstanceElements*;

createResourceMultipleInstance: createResourceInstanceId? createResourceInstanceElements* ';';

createResourceInstanceId: STRING ':'?;

createResourceInstanceElements: createResourceInstanceParam | createResourceInstanceChild;

createResourceInstanceChild: createResource ','?;

createResourceInstanceParam: ID '=' createResourceInstanceParamValue ','?;

createResourceInstanceParamValue: STRING | NB;

packageName: ID ( '.' ID )*;

fullyQualifiedId: ( packageName ':' )? ID ;

ID: [a-zA-Z] [a-zA-Z0-9]*;
NB: ([0-9] | '.')+;
LF: '\n' -> skip;
WS: [ \r\t]+ -> skip;
UQSTRING: [a-zA-Z0-9-_/$%^&*!];
STRING
   : '"' ( ESCAPE | ~('"'|'\\') )* '"'
   | '\'' ( ESCAPE | ~('\''|'\\') )* '\''
   | UQSTRING
   ;
fragment
ESCAPE : '\\' . ;