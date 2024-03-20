package org.apache.xalan.xsltc.compiler.util;

import java.util.ListResourceBundle;

public class ErrorMessages_ko extends ListResourceBundle {
   public Object[][] getContents() {
      return new Object[][]{{"MULTIPLE_STYLESHEET_ERR", "하나 이상의 스타일시트가 동일한 파일에서 정의되었습니다."}, {"TEMPLATE_REDEF_ERR", "''{0}'' 템플리트가 이미 이 스타일시트에서 정의되었습니다."}, {"TEMPLATE_UNDEF_ERR", "''{0}'' 템플리트가 이 스타일시트에서 정의되지 않았습니다."}, {"VARIABLE_REDEF_ERR", "''{0}'' 변수가 동일한 범위 안에서 여러 번 정의되었습니다."}, {"VARIABLE_UNDEF_ERR", "''{0}'' 매개변수 또는 변수가 정의되지 않았습니다."}, {"CLASS_NOT_FOUND_ERR", "''{0}'' 클래스를 찾을 수 없습니다."}, {"METHOD_NOT_FOUND_ERR", "''{0}'' 외부 메소드를 찾을 수 없습니다. (public이어야 합니다.)"}, {"ARGUMENT_CONVERSION_ERR", "''{0}'' 메소드로의 호출에서 인수/리턴 유형을 변환할 수 없습니다."}, {"FILE_NOT_FOUND_ERR", "''{0}'' URI 또는 파일을 찾을 수 없습니다."}, {"INVALID_URI_ERR", "''{0}'' URI가 유효하지 않습니다."}, {"FILE_ACCESS_ERR", "''{0}'' URI 또는 파일을 열 수 없습니다."}, {"MISSING_ROOT_ERR", "<xsl:stylesheet> 또는 <xsl:transform> 요소가 예상됩니다."}, {"NAMESPACE_UNDEF_ERR", "''{0}'' 이름 공간 접두부가 선언되지 않았습니다."}, {"FUNCTION_RESOLVE_ERR", "''{0}'' 함수에 대한 호출을 분석할 수 없습니다."}, {"NEED_LITERAL_ERR", "''{0}''에 대한 인수는 리터럴 문자열이어야 합니다."}, {"XPATH_PARSER_ERR", "''{0}'' XPath 표현식 구문 분석 중에 오류가 발생했습니다."}, {"REQUIRED_ATTR_ERR", "''{0}'' 필수 속성이 누락되었습니다."}, {"ILLEGAL_CHAR_ERR", "XPath 표현식의 ''{0}'' 문자가 유효하지 않습니다."}, {"ILLEGAL_PI_ERR", "처리 명령어에 대한 ''{0}'' 이름이 유효하지 않습니다."}, {"STRAY_ATTRIBUTE_ERR", "''{0}'' 속성이 요소의 외부에 있습니다."}, {"ILLEGAL_ATTRIBUTE_ERR", "''{0}'' 속성이 유효하지 않습니다."}, {"CIRCULAR_INCLUDE_ERR", "import/include가 순환됩니다. ''{0}'' 스타일시트가 이미 로드되었습니다."}, {"RESULT_TREE_SORT_ERR", "결과 트리 단편을 정렬할 수 없습니다. (<xsl:sort> 요소가 무시됩니다.) 결과 트리를 작성할 때 노드를 정렬해야 합니다."}, {"SYMBOLS_REDEF_ERR", "''{0}'' 10진수 포맷팅이 이미 정의되어 있습니다."}, {"XSL_VERSION_ERR", "XSLTC에서 ''{0}'' XSL 버전을 지원하지 않습니다."}, {"CIRCULAR_VARIABLE_ERR", "''{0}''에서 변수/매개변수 참조가 순환됩니다."}, {"ILLEGAL_BINARY_OP_ERR", "2진 표현식에 대한 연산자를 알 수 없습니다."}, {"ILLEGAL_ARG_ERR", "함수 호출에 대한 인수가 유효하지 않습니다."}, {"DOCUMENT_ARG_ERR", "document() 함수에 대한 두 번째 인수는 node-set여야 합니다."}, {"MISSING_WHEN_ERR", "<xsl:choose>에 최소 하나의 <xsl:when> 요소가 필요합니다."}, {"MULTIPLE_OTHERWISE_ERR", "<xsl:choose>에 하나의 <xsl:otherwise> 요소만이 허용됩니다."}, {"STRAY_OTHERWISE_ERR", "<xsl:otherwise>는 <xsl:choose>에서만 사용될 수 있습니다."}, {"STRAY_WHEN_ERR", "<xsl:when>은 <xsl:choose>에서만 사용될 수 있습니다."}, {"WHEN_ELEMENT_ERR", "<xsl:when> 및 <xsl:otherwise> 요소만이 <xsl:choose>에서 허용됩니다."}, {"UNNAMED_ATTRIBSET_ERR", "<xsl:attribute-set>이 'name' 속성에서 누락되었습니다."}, {"ILLEGAL_CHILD_ERR", "하위 요소가 유효하지 않습니다."}, {"ILLEGAL_ELEM_NAME_ERR", "''{0}'' 요소를 호출할 수 없습니다."}, {"ILLEGAL_ATTR_NAME_ERR", "''{0}'' 속성을 호출할 수 없습니다."}, {"ILLEGAL_TEXT_NODE_ERR", "텍스트 데이터가 최상위 레벨 <xsl:stylesheet> 요소의 외부에 있습니다."}, {"SAX_PARSER_CONFIG_ERR", "JAXP 구문 분석기가 제대로 구성되지 않았습니다."}, {"INTERNAL_ERR", "복구할 수 없는 XSLTC-내부 오류: ''{0}''"}, {"UNSUPPORTED_XSL_ERR", "''{0}'' XSL 요소가 지원되지 않습니다."}, {"UNSUPPORTED_EXT_ERR", "''{0}'' XSLTC 확장자를 인식할 수 없습니다."}, {"MISSING_XSLT_URI_ERR", "입력 문서는 스타일시트가 아닙니다. (XSL 이름 공간이 루트 요소에서 선언되지 않았습니다.)"}, {"MISSING_XSLT_TARGET_ERR", "''{0}'' 스타일시트 대상을 찾을 수 없습니다."}, {"NOT_IMPLEMENTED_ERR", "구현되지 않았습니다: ''{0}''"}, {"NOT_STYLESHEET_ERR", "입력 문서에 XSL 스타일시트가 포함되지 않았습니다."}, {"ELEMENT_PARSE_ERR", "''{0}'' 요소를 구문 분석할 수 없습니다."}, {"KEY_USE_ATTR_ERR", "<key>의 use 속성은 node, node-set, string 또는 number여야 합니다."}, {"OUTPUT_VERSION_ERR", "출력 XML 문서 버전은 1.0이어야 합니다."}, {"ILLEGAL_RELAT_OP_ERR", "관계식에 대한 연산자를 알 수 없습니다."}, {"ATTRIBSET_UNDEF_ERR", "존재하지 않는 속성 세트 ''{0}'' 사용을 시도 중입니다."}, {"ATTR_VAL_TEMPLATE_ERR", "''{0}'' 속성값 템플리트를 구문 분석할 수 없습니다."}, {"UNKNOWN_SIG_TYPE_ERR", "''{0}'' 클래스에 대한 서명에 알 수 없는 데이터 유형이 있습니다."}, {"DATA_CONVERSION_ERR", "데이터 유형을 ''{0}''에서 ''{1}''(으)로 변환할 수 없습니다."}, {"NO_TRANSLET_CLASS_ERR", "이 Templates에는 유효한 translet 클래스 정의가 포함되어 있지 않습니다."}, {"NO_MAIN_TRANSLET_ERR", "이 Templates에는 ''{0}'' 이름인 클래스가 포함되어 있지 않습니다."}, {"TRANSLET_CLASS_ERR", "''{0}'' translet 클래스를 로드할 수 없습니다."}, {"TRANSLET_OBJECT_ERR", "translet 클래스가 로드되었지만 translet 인스턴스를 작성할 수 없습니다."}, {"ERROR_LISTENER_NULL_ERR", "''{0}''에 대한 ErrorListener를 널(null)로 설정하려고 합니다."}, {"JAXP_UNKNOWN_SOURCE_ERR", "XSLTC에서 StreamSource, SAXSource 및 DOMSource만을 지원합니다."}, {"JAXP_NO_SOURCE_ERR", "''{0}''(으)로 패스된 Source 오브젝트에 컨텐츠가 없습니다."}, {"JAXP_COMPILE_ERR", "스타일시트를 컴파일할 수 없습니다."}, {"JAXP_INVALID_ATTR_ERR", "TransformerFactory ''{0}'' 속성을 인식할 수 없습니다."}, {"JAXP_SET_RESULT_ERR", "setResult()는 startDocument()보다 먼저 호출되어야 합니다."}, {"JAXP_NO_TRANSLET_ERR", "Transformer에 요약된 translet 오브젝트가 없습니다."}, {"JAXP_NO_HANDLER_ERR", "변환 결과에 대한 출력 핸들러가 정의되지 않았습니다."}, {"JAXP_NO_RESULT_ERR", "''{0}''(으)로 패스된 Result 오브젝트가 유효하지 않습니다."}, {"JAXP_UNKNOWN_PROP_ERR", "''{0}'' 잘못된 Transformer 특성에 액세스하려고 합니다."}, {"SAX2DOM_ADAPTER_ERR", "SAX2DOM ''{0}'' 어댑터를 작성할 수 없습니다."}, {"XSLTC_SOURCE_ERR", "XSLTCSource.build()가 설정된 시스템 ID 없이 호출되었습니다."}, {"ER_RESULT_NULL", "결과는 널(null)이 될 수 없습니다."}, {"JAXP_INVALID_SET_PARAM_VALUE", "{0} 매개변수 값은 유효한 Java 오브젝트여야 합니다."}, {"COMPILE_STDIN_ERR", "-i 옵션은 -o 옵션과 함께 사용되어야 합니다."}, {"COMPILE_USAGE_STR", "SYNOPSIS\n java org.apache.xalan.xsltc.cmdline.Compile [-o <output>]\n [-d <directory>] [-j <jarfile>] [-p <package>]\n [-n] [-x] [-u] [-v] [-h] { <stylesheet> | -i }\n\n 옵션\n -o <output>    생성된 Translet에 <output> 이름을 지정합니다. \n                기본적으로 Translet 이름을 <stylesheet> 이름에서\n 가져옵니다. 이 옵션은 여러 개의 스타일시트를 \n 컴파일할 경우 무시됩니다.\n -d <directory> Translet의 대상 디렉토리를 지정합니다.\n -j <jarfile>   <jarfile>로 지정된 jar 파일 이름으로\n Translet 클래스를 패키지합니다.\n -p <package>   생성된 모든 Translet 클래스에 대해 패키지 이름 접두부를\n 지정합니다.\n -n             템플리트 인라이닝(평균보다 우수한)을\n 사용 가능하게 합니다.\n -x             추가 디버깅 메시지 출력을 시작합니다.\n -u             <stylesheet> 인수를 URL로 해석합니다.\n -i             stdin으로부터 스타일시트를 읽도록 컴파일러를 강제 실행합니다.\n -v             컴파일러 버전을 인쇄합니다.\n -h             사용법 명령문을 인쇄합니다.\n"}, {"TRANSFORM_USAGE_STR", "SYNOPSIS \n java org.apache.xalan.xsltc.cmdline.Transform [-j <jarfile>]\n [-x] [-n <iterations>] {-u <document_url> | <document>}\n <class> [<param1>=<value1> ...]\n\n Translet <class>를 사용하여 <document>로 지정된 XML 문서를 \n 변환합니다. Translet <class>는 사용자의 CLASSPATH 또는\n 선택적으로 지정된 <jarfile> 내에 있습니다.\n옵션\n -j <jarfile>      Translet을 로드해올 jarfile을 지정합니다.\n -x                추가 디버깅 메시지 출력을 시작합니다.\n -n <iterations>   <iterations> 차례 변환을 실행하며\n 프로파일링 정보를 표시합니다.\n -u <document_url> XML 입력 문서를 URL로 지정합니다.\n"}, {"STRAY_SORT_ERR", "<xsl:sort>는 <xsl:for-each> 또는 <xsl:apply-templates>에서만 사용될 수 있습니다."}, {"UNSUPPORTED_ENCODING", "이 JVM에서 ''{0}'' 출력 인코딩을 지원하지 않습니다."}, {"SYNTAX_ERR", "''{0}''에 구문 오류가 있습니다."}, {"CONSTRUCTOR_NOT_FOUND", "''{0}'' 외부 구성자를 찾을 수 없습니다."}, {"NO_JAVA_FUNCT_THIS_REF", "non-static Java 함수 ''{0}''의 첫 번째 인수가 유효한 오브젝트 참조가 아닙니다."}, {"TYPE_CHECK_ERR", "''{0}'' 표현식의 유형을 검사하는 중에 오류가 발생했습니다."}, {"TYPE_CHECK_UNK_LOC_ERR", "알 수 없는 위치에서 표현식의 유형을 검사하는 중에 오류가 발생했습니다."}, {"ILLEGAL_CMDLINE_OPTION_ERR", "''{0}'' 명령행 옵션이 유효하지 않습니다."}, {"CMDLINE_OPT_MISSING_ARG_ERR", "''{0}'' 명령행 옵션에 필수 인수가 누락되었습니다."}, {"WARNING_PLUS_WRAPPED_MSG", "경고:  ''{0}''\n       :{1}"}, {"WARNING_MSG", "경고:  ''{0}''"}, {"FATAL_ERR_PLUS_WRAPPED_MSG", "심각한 오류:  ''{0}''\n           :{1}"}, {"FATAL_ERR_MSG", "심각한 오류:  ''{0}''"}, {"ERROR_PLUS_WRAPPED_MSG", "오류:  ''{0}''\n     :{1}"}, {"ERROR_MSG", "오류:  ''{0}''"}, {"TRANSFORM_WITH_TRANSLET_STR", "''{0}'' translet을 사용하여 변환하십시오."}, {"TRANSFORM_WITH_JAR_STR", "''{1}'' jar 파일의 ''{0}'' Translet을 사용하여 변환하십시오."}, {"COULD_NOT_CREATE_TRANS_FACT", "TransformerFactory 클래스 ''{0}''의 인스턴스를 작성할 수 없습니다."}, {"TRANSLET_NAME_JAVA_CONFLICT", "''{0}'' 이름은 Java 클래스 이름에서 사용할 수 없는 문자를 포함하고 있으므로 Translet 클래스 이름으로 사용할 수 없습니다. 대신에 ''{1}'' 이름이 사용되었습니다."}, {"COMPILER_ERROR_KEY", "컴파일러 오류:"}, {"COMPILER_WARNING_KEY", "컴파일러 경고:"}, {"RUNTIME_ERROR_KEY", "Translet 오류:"}, {"INVALID_QNAME_ERR", "값이 QName 또는 QName의 화이트 스페이스로 구분된 목록이어야 하는 속성에 ''{0}'' 값이 있습니다."}, {"INVALID_NCNAME_ERR", "값이 NCName이어야 하는 속성에 ''{0}'' 값이 있습니다."}, {"INVALID_METHOD_IN_OUTPUT", "<xsl:output> 요소의 메소드 속성에 ''{0}'' 값이 있습니다. 값은 ''xml'', ''html'', ''text'' 또는 qname-but-not-ncname 중 하나여야 합니다."}, {"JAXP_GET_FEATURE_NULL_NAME", "TransformerFactory.getFeature(문자열 이름)에서 기능 이름이 널(null)이면 안됩니다."}, {"JAXP_SET_FEATURE_NULL_NAME", "TransformerFactory.setFeature(문자열 이름, 부울 값)에서 기능 이름이 널(null)이면 안됩니다."}, {"JAXP_UNSUPPORTED_FEATURE", "이 TransformerFactory에서 ''{0}'' 기능을 설정할 수 없습니다."}};
   }
}
