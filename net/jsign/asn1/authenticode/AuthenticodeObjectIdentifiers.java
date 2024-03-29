package net.jsign.asn1.authenticode;

import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface AuthenticodeObjectIdentifiers {
   ASN1ObjectIdentifier Authenticode = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2");
   ASN1ObjectIdentifier SPC_INDIRECT_DATA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.4");
   ASN1ObjectIdentifier SPC_SP_AGENCY_INFO_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.10");
   ASN1ObjectIdentifier SPC_STATEMENT_TYPE_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.11");
   ASN1ObjectIdentifier SPC_SP_OPUS_INFO_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.12");
   ASN1ObjectIdentifier SPC_CERT_EXTENSIONS_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.14");
   ASN1ObjectIdentifier SPC_PE_IMAGE_DATA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.15");
   ASN1ObjectIdentifier SPC_RAW_FILE_DATA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.18");
   ASN1ObjectIdentifier SPC_STRUCTURED_STORAGE_DATA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.19");
   ASN1ObjectIdentifier SPC_JAVA_CLASS_DATA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.20");
   ASN1ObjectIdentifier SPC_INDIVIDUAL_SP_KEY_PURPOSE_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.21");
   ASN1ObjectIdentifier SPC_COMMERCIAL_SP_KEY_PURPOSE_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.22");
   ASN1ObjectIdentifier SPC_CAB_DATA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.25");
   ASN1ObjectIdentifier SPC_GLUE_RDN_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.25");
   ASN1ObjectIdentifier SPC_MINIMAL_CRITERIA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.26");
   ASN1ObjectIdentifier SPC_FINANCIAL_CRITERIA_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.27");
   ASN1ObjectIdentifier SPC_LINK_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.28");
   ASN1ObjectIdentifier SPC_HASH_INFO_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.29");
   ASN1ObjectIdentifier SPC_SIPINFO_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.1.30");
   ASN1ObjectIdentifier SPC_NESTED_SIGNATURE_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.4.1");
   ASN1ObjectIdentifier SPC_TIME_STAMP_REQUEST_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.3.2.1");
   ASN1ObjectIdentifier SPC_RFC3161_OBJID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.3.3.1");
}
