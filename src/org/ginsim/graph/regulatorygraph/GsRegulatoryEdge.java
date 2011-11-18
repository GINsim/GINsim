package org.ginsim.graph.regulatorygraph;

import org.ginsim.annotation.Annotation;


public class GsRegulatoryEdge {
	public byte threshold;
	public byte sign;
	
	public byte index;
	public GsRegulatoryMultiEdge me;
	
	Annotation annotation = new Annotation();
	
	public GsRegulatoryEdge(GsRegulatoryMultiEdge me) {
		this.me = me;
	}
	
	public Object clone(GsRegulatoryMultiEdge me) {
		GsRegulatoryEdge clone = new GsRegulatoryEdge(me);
		clone.threshold = threshold;
		clone.index = index;
		clone.sign = sign;
		clone.annotation = (Annotation)annotation.clone();
		return clone;
	}
	
	public Annotation getAnnotation() {
		return annotation;
	}
	
	public byte getMin() {
		return threshold;
	}
	
	public byte getMax() {
		return me.getMax(index);
	}

	public String toString() {
		return getShortDetail(" ");
	}
	public String getShortInfo(String separator) {
		return me.getSource()+getStringThreshold(separator);
	}
	public String getLongInfo(String separator) {
		return me.getSource()+separator+me.getTarget()+getStringIndex(separator);
	}
	public String getShortDetail(String separator) {
		return getShortInfo(":")+separator+getRangeAndSign();
	}
	public String getShortDetail() {
		return getShortInfo() + " " + getRangeAndSign();
	}
	public String getShortInfo() {
		return me.getSource() + ":" + threshold;
	}
	public String getLongDetail(String separator) {
		return getLongInfo(separator)+separator+getRangeAndSign();
	}
	private String getStringIndex(String prefix) {
		if (me.getEdgeCount() == 1) {
			return "";
		}
		return prefix+(index+1);
	}
	private String getStringThreshold(String prefix) {
		if (threshold == 1) {
			return "";
		}
		return prefix+threshold;
	}
	private String getRangeAndSign() {
		return "["+threshold+","+getMaxAsString()+"] ; "+GsRegulatoryMultiEdge.SIGN[sign];
	}
	public String getMaxAsString() {
		String smax;
		if (index == me.getEdgeCount()-1) {
			smax = "max";
		} else if (me.getMin(index+1)> threshold) {
			smax = ""+(me.getMin(index+1)-1);
		} else {
			smax = "INVALID";
		}
		return smax;
	}
}