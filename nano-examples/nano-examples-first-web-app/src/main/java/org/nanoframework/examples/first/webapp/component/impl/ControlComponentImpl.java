package org.nanoframework.examples.first.webapp.component.impl;

import javax.servlet.http.HttpServletRequest;

import org.nanoframework.core.status.ResultMap;
import org.nanoframework.examples.first.webapp.component.ControlComponent;
import org.nanoframework.web.server.http.status.HttpStatusCode;

public class ControlComponentImpl implements ControlComponent {
	
	@Override
	public Object shutdown(HttpServletRequest request) {
		if("localhost".equals(request.getServerName())) {
			new Thread(() -> {
				try { Thread.sleep(3000L); } catch(InterruptedException e) { }
				System.exit(0);
			}).start();
			
			return ResultMap.create(HttpStatusCode.SC_OK, "系统即将停止", "SUCCESS");
		} else 
			return ResultMap.create(HttpStatusCode.SC_BAD_REQUEST, "不支持外部请求", "ERROR");
	}

}