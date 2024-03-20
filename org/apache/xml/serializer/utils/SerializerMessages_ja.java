package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_ja extends ListResourceBundle {
   public Object[][] getContents() {
      Object[][] contents = new Object[][]{{"BAD_MSGKEY", "メッセージ・キー ''{0}'' はメッセージ・クラス ''{1}'' にありません。"}, {"BAD_MSGFORMAT", "メッセージ・クラス ''{1}'' のメッセージ ''{0}'' のフォーマット設定が失敗しました。"}, {"ER_SERIALIZER_NOT_CONTENTHANDLER", "シリアライザー・クラス ''{0}'' は org.xml.sax.ContentHandler を実装しません。"}, {"ER_RESOURCE_COULD_NOT_FIND", "リソース [ {0} ] は見つかりませんでした。\n {1}"}, {"ER_RESOURCE_COULD_NOT_LOAD", "リソース [ {0} ] をロードできませんでした: {1} \n {2} \t {3}"}, {"ER_BUFFER_SIZE_LESSTHAN_ZERO", "バッファー・サイズ <=0"}, {"ER_INVALID_UTF16_SURROGATE", "無効な UTF-16 サロゲートが検出されました: {0} ?"}, {"ER_OIERROR", "入出力エラー"}, {"ER_ILLEGAL_ATTRIBUTE_POSITION", "下位ノードの後または要素が生成される前に属性 {0} は追加できません。  属性は無視されます。"}, {"ER_NAMESPACE_PREFIX", "接頭部 ''{0}'' の名前空間が宣言されていません。"}, {"ER_STRAY_ATTRIBUTE", "属性 ''{0}'' が要素の外側です。"}, {"ER_STRAY_NAMESPACE", "名前空間宣言 ''{0}''=''{1}'' が要素の外側です。"}, {"ER_COULD_NOT_LOAD_RESOURCE", "''{0}'' をロードできませんでした (CLASSPATH を確認してください)。現在はデフォルトのもののみを使用しています。"}, {"ER_ILLEGAL_CHARACTER", "{1} の指定された出力エンコードで表せない整数値 {0} の文字の出力を試みました。"}, {"ER_COULD_NOT_LOAD_METHOD_PROPERTY", "出力メソッド ''{1}'' のプロパティー・ファイル ''{0}'' をロードできませんでした (CLASSPATH を確認してください)"}, {"ER_INVALID_PORT", "無効なポート番号"}, {"ER_PORT_WHEN_HOST_NULL", "ホストがヌルであるとポートを設定できません"}, {"ER_HOST_ADDRESS_NOT_WELLFORMED", "ホストはうまく構成されたアドレスでありません"}, {"ER_SCHEME_NOT_CONFORMANT", "スキームは一致していません。"}, {"ER_SCHEME_FROM_NULL_STRING", "ヌル・ストリングからはスキームを設定できません"}, {"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "パスに無効なエスケープ・シーケンスが含まれています"}, {"ER_PATH_INVALID_CHAR", "パスに無効文字: {0} が含まれています"}, {"ER_FRAG_INVALID_CHAR", "フラグメントに無効文字が含まれています"}, {"ER_FRAG_WHEN_PATH_NULL", "パスがヌルであるとフラグメントを設定できません"}, {"ER_FRAG_FOR_GENERIC_URI", "総称 URI のフラグメントしか設定できません"}, {"ER_NO_SCHEME_IN_URI", "スキームは URI で見つかりません"}, {"ER_CANNOT_INIT_URI_EMPTY_PARMS", "URI は空のパラメーターを使用して初期化できません"}, {"ER_NO_FRAGMENT_STRING_IN_PATH", "フラグメントはパスとフラグメントの両方に指定できません"}, {"ER_NO_QUERY_STRING_IN_PATH", "照会ストリングはパスおよび照会ストリング内に指定できません"}, {"ER_NO_PORT_IF_NO_HOST", "ホストが指定されていない場合はポートを指定してはいけません"}, {"ER_NO_USERINFO_IF_NO_HOST", "ホストが指定されていない場合は Userinfo を指定してはいけません"}, {"ER_XML_VERSION_NOT_SUPPORTED", "警告: 出力文書のバージョンとして ''{0}'' が要求されました。  このバージョンの XML はサポートされません。  出力文書のバージョンは ''1.0'' になります。"}, {"ER_SCHEME_REQUIRED", "スキームが必要です。"}, {"ER_FACTORY_PROPERTY_MISSING", "SerializerFactory に渡された Properties オブジェクトには ''{0}'' プロパティーがありません。"}, {"ER_ENCODING_NOT_SUPPORTED", "警告:  エンコード ''{0}'' はこの Java ランタイムではサポートされていません。"}, {"FEATURE_NOT_FOUND", "パラメーター ''{0}'' は認識されません。"}, {"FEATURE_NOT_SUPPORTED", "パラメーター ''{0}'' は認識されますが、要求された値は設定できません。"}, {"DOMSTRING_SIZE_ERR", "結果のストリングが長すぎるため、DOMString 内に収まりません: ''{0}''。"}, {"TYPE_MISMATCH_ERR", "このパラメーター名の値の型は、期待される値の型と不適合です。"}, {"no-output-specified", "書き込まれるデータの出力宛先がヌルです。"}, {"unsupported-encoding", "サポートされないエンコードが検出されました。"}, {"ER_UNABLE_TO_SERIALIZE_NODE", "ノードを直列化できませんでした。"}, {"cdata-sections-splitted", "CDATA セクションに 1 つ以上の終了マーカー ']]>' が含まれています。"}, {"ER_WARNING_WF_NOT_CHECKED", "整形式性チェッカーのインスタンスを作成できませんでした。  well-formed パラメーターの設定は true でしたが、整形式性の検査は実行できません。"}, {"wf-invalid-character", "ノード ''{0}'' に無効な XML 文字があります。"}, {"ER_WF_INVALID_CHARACTER_IN_COMMENT", "コメントの中に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"}, {"ER_WF_INVALID_CHARACTER_IN_PI", "処理命令データの中に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"}, {"ER_WF_INVALID_CHARACTER_IN_CDATA", "CDATA セクションの中に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"}, {"ER_WF_INVALID_CHARACTER_IN_TEXT", "ノードの文字データの内容に無効な XML 文字 (Unicode: 0x{0}) が見つかりました。"}, {"wf-invalid-character-in-node-name", "''{1}'' という名前の {0} ノードの中に無効な XML 文字が見つかりました。"}, {"ER_WF_DASH_IN_COMMENT", "ストリング \"--\" はコメント内では使用できません。"}, {"ER_WF_LT_IN_ATTVAL", "要素型 \"{0}\" に関連した属性 \"{1}\" の値には ''<'' 文字を含めてはいけません。"}, {"ER_WF_REF_TO_UNPARSED_ENT", "解析対象外実体参照 \"&{0};\" は許可されません。"}, {"ER_WF_REF_TO_EXTERNAL_ENT", "属性値での外部実体参照 \"&{0};\" は許可されません。"}, {"ER_NS_PREFIX_CANNOT_BE_BOUND", "接頭部 \"{0}\" は名前空間 \"{1}\" に結合できません。"}, {"ER_NULL_LOCAL_ELEMENT_NAME", "要素 \"{0}\" のローカル名がヌルです。"}, {"ER_NULL_LOCAL_ATTR_NAME", "属性 \"{0}\" のローカル名がヌルです。"}, {"unbound-prefix-in-entity-reference", "実体ノード \"{0}\" の置換テキストに、未結合の接頭部 \"{2}\" を持つ要素ノード \"{1}\" が含まれています。"}, {"unbound-prefix-in-entity-reference", "実体ノード \"{0}\" の置換テキストに、未結合の接頭部 \"{2}\" を持つ属性ノード \"{1}\" が含まれています。"}};
      return contents;
   }
}