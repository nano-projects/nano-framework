/**
 * Copyright 2015- the original author or authors.
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
package org.nanoframework.extension.concurrent.quartz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务调度注解
 * 
 * @author yanghe
 * @date 2015年6月11日 下午2:32:03 
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Quartz {
	
	/**
	 * 调度任务名称
	 * @return String
	 */
	String name();
	
	/**
	 * 调度任务before()和after()方法是否在整个任务生命周期中只执行一次, 为true时只执行, 默认: false
	 * @return boolean
	 */
	boolean beforeAfterOnly() default false;
	
	/**
	 * 任务执行次数, 为0时无限执行, 直到被主动关闭 默认: 0
	 * @return int
	 */
	int runNumberOfTimes() default 0;
	
	/**
	 * 执行间隔时间, 单位: 毫秒, 默认时间: 0
	 * @return int
	 */
	int interval() default 0;
	
	/**
	 * 并行度，调度任务线程数, 并行度必须大于等于0 , 默认并行度: 0
	 * @return int
	 */
	int parallel() default 0;
	
	/**
	 * 通过属性文件设置并行度, 如果设置了此属性并且可以整合获取到属性值，则使用此属性设置，并无视parallel的设置, 默认值: ""
	 * @return String
	 */
	String parallelProperty() default "";
	
	/**
	 * 采用核心并行度，并无视parallel及parallelProperty的设置, 默认值: false
	 * @return boolean
	 */
	boolean coreParallel() default false;
	
	/**
	 * 周(1~7: 7=周日) <br>
	 * 月(1~12) <br>
	 * 日(1~31: 每月不足31日的自动使用当月的最后一天) <br>
	 * 时(0~23) <br>
	 * 分(0~59) <br>
	 * 秒(0~59) <br>
	 * 
	 * @return String
	 */
	String crontab() default "* * * * * *";
}
