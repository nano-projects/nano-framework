package org.nanoframework.extension.concurrent.quartz;

import org.nanoframework.commons.entity.BaseEntity;

@Deprecated
public class QuartzStatus extends BaseEntity {
	private static final long serialVersionUID = 5876395587017572488L;

	private Long key;
	private String group;
	private String id;
	private Status status;

	public QuartzStatus() {
	}

	public QuartzStatus(String group, String id, Status status) {
		this.group = group;
		this.id = id;
		this.status = status;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public enum Status {
		STARTED, STOPPING, STOPPED;
	}
}
