package org.nanoframework.core.httpclient;

import org.nanoframework.commons.entity.BaseEntity;

public class HttpResponse extends BaseEntity {
	private static final long serialVersionUID = -3602195552991064086L;

	public static final HttpResponse EMPTY = create(0, "", "");

	public final int statusCode;
	public final String reasonPhrase;
	public final String entity;

	public HttpResponse(int statusCode, String reasonPhrase, String entity) {
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
		this.entity = entity;
	}

	public static final HttpResponse create(int statusCode, String reasonPhrase, String entity) {
		return new HttpResponse(statusCode, reasonPhrase, entity);
	}

}