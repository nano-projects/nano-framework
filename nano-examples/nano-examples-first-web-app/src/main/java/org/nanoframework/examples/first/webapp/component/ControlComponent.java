package org.nanoframework.examples.first.webapp.component;

import javax.servlet.http.HttpServletRequest;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.examples.first.webapp.component.impl.ControlComponentImpl;

import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(ControlComponentImpl.class)
@RequestMapping("/sys")
public interface ControlComponent {
	
	@RequestMapping("/shutdown")
	public Object shutdown(HttpServletRequest request);
	
}