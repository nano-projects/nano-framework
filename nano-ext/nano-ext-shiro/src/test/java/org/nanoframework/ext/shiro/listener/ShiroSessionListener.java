/**
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.ext.shiro.listener;

import java.sql.Timestamp;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * @author yanghe
 * @date 2015年12月10日 上午9:32:11
 */
public class ShiroSessionListener implements SessionListener {
	private Logger LOG = LoggerFactory.getLogger(ShiroSessionListener.class);
	
	@Override
	public void onStart(Session session) {
		LOG.debug("Session Start: " + session.getId() + ", host: " + session.getHost() + ", startTime: " + (new Timestamp(session.getStartTimestamp().getTime())) 
				+ ", last access time: " + (new Timestamp(session.getLastAccessTime().getTime())));
	}

	@Override
	public void onStop(Session session) {
		LOG.debug("Session Stop: " + session.getId() + ", host: " + session.getHost() + ", startTime: " + (new Timestamp(session.getStartTimestamp().getTime())) 
				+ ", last access time: " + (new Timestamp(session.getLastAccessTime().getTime())));
	}

	@Override
	public void onExpiration(Session session) {
		LOG.debug("Session Expiration: " + session.getId() + ", host: " + session.getHost() + ", startTime: " + (new Timestamp(session.getStartTimestamp().getTime())) 
				+ ", last access time: " + (new Timestamp(session.getLastAccessTime().getTime())));
	}

}
