package org.nanoframework.core;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.UUIDUtils;

import com.google.inject.Singleton;

@Singleton
public class EntitySingleton extends BaseEntity {
	private static final long serialVersionUID = 2887355674297694472L;
	public final String uuid = UUIDUtils.create();
}