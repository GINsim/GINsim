package org.ginsim.gui.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ginsim.common.OpenHelper;
import org.ginsim.common.utils.IOUtils;




public class HttpHelper implements OpenHelper {

  static Map m_proto = new HashMap();

  public boolean open(String proto, String value) {
    return GUIIOUtils.openURI(getLink(proto, value));
  }
  public void add(String proto, String value) {
  }

  public static void setup() {
    m_proto.put("http", "http:");
    m_proto.put("wp", "http://en.wikipedia.org/wiki/");

    m_proto.put("doi", "http://dx.doi.org/");
    
    m_proto.put("pubmed", "http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=retrieve&db=pubmed&dopt=AbstractPlus&list_uids=");
    m_proto.put("pmid", "http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=retrieve&db=pubmed&dopt=AbstractPlus&list_uids=");
    m_proto.put("PMID", "http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=retrieve&db=pubmed&dopt=AbstractPlus&list_uids=");

    m_proto.put("hugo", "http://www.genenames.org/data/hgnc_data.php?hgnc_id=");
    m_proto.put("entrez", "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&ordinalpos=1&TermToSearch=");

    m_proto.put("swissprot", "http://www.expasy.org/cgi-bin/idtracker?id=");
    m_proto.put("refseq", "http://www.ncbi.nlm.nih.gov/sites/gquery?term=");
    m_proto.put("uniprot", "http://www.uniprot.org/uniprot/");

    HttpHelper h = new HttpHelper();
    Iterator it = m_proto.keySet().iterator();
    while (it.hasNext()) {
      IOUtils.addHelperClass((String)it.next(), h);
    }
  }
  public String getLink(String proto, String value) {
    return m_proto.get(proto)+value;
  }
}