package org.ginsim.core.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ginsim.common.utils.ListReorderListener;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.graph.Graph;

/**
 * Generic annotation: some free text and a list of urls.
 * 
 * @author Aurelien Naldi
 */
public class Annotation extends ArrayList<AnnotationLink> implements XMLize, ListReorderListener {

	private String comment;
	Pattern ref = Pattern.compile("\\[([0-9][0-9,\\w]*)\\]");
	
	/**
	 * create an empty annotation.
	 */
	public Annotation() {
		comment = "";
	}

	/**
	 * Getter list link
	 * @return list of annotation
	 */
	public List<AnnotationLink> getLinkList() {
        return this;
    }
    
	/**
	 * get the vector of url
	 * @param index indice int
	 * @return the vector of String (url).
	 */
	public String getLink(int index) {
		return get(index).toString();
	}

	/**
	 * open link function
	 * @param index indice
	 */
	public void openLink(int index) {
		get(index).open();
	}

	/**
	 * add link graph
	 * @param s string to add
	 * @param graph the graph
	 */
	public void addLink(String s, Graph graph) {
		setLink(s, size(), graph);
	}

	/**
	 * Delete link function
	 * @param s string link
	 * @param graph the graph
	 */
	public void delLink(String s, Graph graph) {
		while (remove(new AnnotationLink(s, graph))) ;
	}

	/**
	 * Stter for link
	 * @param s  string link
	 * @param index indice int
	 * @param graph the graph
	 */
	public void setLink(String s, int index, Graph graph) {
		if (index == size()) {
			AnnotationLink al = new AnnotationLink(s, graph);
			if (!containsLink(al)) add(al);
		} else {
			(get(index)).setText(s, graph);
		}
	}

	/**
	 * Test function if contains link
	 * @param al the AnnotationLink
	 * @return boolean if contains link
	 */
	public boolean containsLink(AnnotationLink al) {
		return contains(al);
	}

	/**
	 * Test if contains link
	 * @param s string link
	 * @return boolean if contains link
	 */
	public boolean containsLink(String s) {
		int nblinks = size();
		for (int i=0 ; i<nblinks ; i++) {
			if (getLink(i).equals(s)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * return the String containing the comments.
	 * @return a String
	 */
	public String getComment() {
		return comment;
	}


	/**
	 * Set the string containing the comments.
	 * @param comment the new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Add the string containing the comments to the end of the actual comments.
	 * @param comment the new comment
	 */
	public void appendToComment(String comment) {
		if (this.comment.length() > 0 && !this.comment.endsWith("\n")) {
			this.comment += "\n" + comment;
		} else {
			this.comment += comment;
		}
	}

	@Override
	public void toXML(XMLWriter out) throws IOException {
		if (comment.equals("") && size()==0) {
		    return;         
        }
		out.openTag("annotation");
        if (size() > 0) {
            out.openTag("linklist");
            for (int i=0 ; i<size() ; i++) {
                out.openTag("link");
                out.addAttr("xlink:href", get(i).toString());
                out.closeTag();
            }
            out.closeTag();
        }
		if (!comment.equals("")) {
			out.openTag("comment");
			out.addContent(comment);
			out.closeTag();
		}
        out.closeTag();
	}

	/**
	 * Clone function new Annotation
	 * @return cloned object
	 */
	public Object clone() {
		Annotation clone = new Annotation();
		clone.copyFrom(this);
		return clone;
	}

	/**
	 * Copy Annotation function
	 * @param other a Annotation
	 */
	public void copyFrom(Annotation other) {
		int len = other.size();
		for (int i=0 ; i<len ; i++) {
			addLink(other.getLink(i), null);
		}
		setComment(other.comment);
	}
	
    /**
	 * Test if empty
     * @return true if the annotation is empty
     */
    public boolean isEmpty() {
        return "".equals(comment) && size() == 0;
    }

	/**
	 * reteur a html string comment
	 * @return comment string
	 */
	public String getHTMLComment() {
		StringBuffer buf = new StringBuffer();
		boolean hasLink = false;
		for (AnnotationLink lnk: this ) {
			if (lnk.getHelper() != null) {
				String s = lnk.getHelper().getLink(lnk.proto, lnk.value);
				if (s != null) {
					if (!hasLink) {
						hasLink = true;
						buf.append("<ul>\n");
					}
					if (s == lnk.toString() && s.length() >= 50) {
						buf.append("<li><a href='" + s +"'>" + s.substring(0, 45) + "...</a></li>\n");
					} else {
						buf.append("<li><a href='" + s +"'>" + lnk + "</a></li>\n");
					}
				}
			}
		}
		if (hasLink) {
			buf.append("</ul>\n<p/>");
		}
		buf.append(comment.replaceAll("\n", "<br/>"));
		return buf.toString();
	}
	
	@Override
	public void reordered(int[] mapping) {
		if (comment == null || comment.length() < 3) {
			return;
		}
		int[] mapped = new int[mapping.length];
		for (int i=0 ; i< mapping.length ; i++) {
			mapped[mapping[i]] = i;
		}
		remap(mapped);
	}
	
	@Override
	public void deleted(int[] sel) {
		int[] mapped = new int[size() + sel.length];
		for (int i=0 ; i< mapped.length ; i++) {
			mapped[i] = i;
		}
		for (int d: sel) {
			mapped[d] = -1;
			for (int i=d+1 ; i<mapped.length ; i++) {
				mapped[i]--;
			}
		}
		remap(mapped);
	}
	
	private void remap(int[] mapped) {
		// Find and update references to links in the text
		int len = comment.length();
		Matcher m = ref.matcher(comment);
		StringBuffer newComment = new StringBuffer(len);
		int start = 0;
		while (m.find()) {
			String curMatch = m.group(1);
			int curStart = m.start();
			int curEnd = m.end();
			newComment.append(comment.substring(start, curStart));
			newComment.append("[");
			String[] refs = curMatch.split(",");
			boolean first = true;
			for (String ref: refs) {
				if (!first) {
					newComment.append(",");
				} else {
					first = false;
				}
				int index = Integer.parseInt(ref);
				if (index > 0 && index <= mapped.length) {
					index = mapped[index-1]+1;
				}
				if (index <= 0) {
					newComment.append("?");
				} else {
					newComment.append(index);
				}
			}
			newComment.append("]");
			start = curEnd;
		}
		
		if (start > 0) {
			newComment.append(comment.substring(start));
			comment = newComment.toString();
		}
	}

}
