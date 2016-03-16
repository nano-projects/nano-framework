package org.nanoframework.core;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.UUIDUtils;

public class Entity extends BaseEntity {
	private static final long serialVersionUID = 2887355674297694472L;
	public final String uuid = UUIDUtils.create();
}