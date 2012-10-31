package org.biosemantics.brat;

public class BratEntityAnnotation implements BratAnnotation {

	private static final long serialVersionUID = -6695215317582286121L;
	private String id;
	private String entity;
	private int startPos;
	private int endPos;
	private String text;
	private String fileName;

	public BratEntityAnnotation(String id, String entity, int startPos, int endPos, String text, String fileName) {
		super();
		this.id = id;
		this.entity = entity;
		this.startPos = startPos;
		this.endPos = endPos;
		this.text = text;
		this.fileName = fileName;
	}

	public BratAnnotationType getType() {
		return BratAnnotationType.ENTITY;
	}

	public String getEntity() {
		return entity;
	}

	public String getId() {
		return id;
	}

	public int getStartPos() {
		return startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public String getText() {
		return text;
	}

	public String getFileName() {
		return fileName;
	}

	public String[] toStringArray() {
		return new String[] { id, entity, "" + startPos, "" + endPos, text, fileName };
	}

}
