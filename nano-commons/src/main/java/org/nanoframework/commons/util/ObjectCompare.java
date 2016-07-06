/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.commons.util;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 对象比较处理类
 * 
 * @author yanghe
 * @since 1.0
 */
public class ObjectCompare {

    /**
     * 判断目标值是否存在与源列表中
     * 
     * @param target 源
     * @param source 目标
     * @return 返回是否存在结果 true=存在，false=不存在
     */
    public static final boolean isInList(Object target, Object... source) {
        if (target == null)
            return false;

        if (source != null && source.length > 0) {
            for (Object src : source) {
                if (target.equals(src))
                    return true;
            }
        }

        return false;
    }

    /**
     * 判断目标值是否存在与源列表中
     * 
     * @param target 源
     * @param source 目标
     * @return 返回是否存在结果 true=存在，false=不存在
     */
    public static final boolean isInList(Object target, String... source) {
        if (target == null)
            return false;

        if (source != null && source.length > 0) {
            for (String src : source) {
                if (StringUtils.isEmpty(src))
                    return false;

                if (target.equals(src.trim()))
                    return true;
            }
        }

        return false;
    }

    /**
     * 正则表达式比较target是否在regExs内
     * 
     * @param target 源
     * @param regExs 正则列表
     * @return 返回是否存在结果 true=存在，false=不存在
     */
    public static final boolean isInListByRegEx(String target, String... regExs) {
        if (StringUtils.isBlank(target))
            return false;

        if (regExs != null && regExs.length > 0) {
            for (String regEx : regExs) {
                if (StringUtils.isBlank(regEx))
                    continue;

                if (Pattern.compile(regEx).matcher(target).find())
                    return true;
            }
        }

        return false;
    }

    public static final boolean isInListByRegEx(String target, Set<String> regExs) {
        if (CollectionUtils.isEmpty(regExs))
            return false;

        return isInListByRegEx(target, regExs.toArray(new String[regExs.size()]));
    }

    public static final boolean isInEndWiths(String target, String... source) {
        if (target == null)
            return false;

        if (source != null && source.length > 0) {
            for (String suffix : source) {
                if (target.endsWith(suffix))
                    return true;
            }
        }

        return false;
    }

}
