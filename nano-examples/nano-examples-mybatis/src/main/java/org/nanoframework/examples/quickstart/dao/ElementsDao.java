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
package org.nanoframework.examples.quickstart.dao;

import java.util.List;

import org.nanoframework.examples.quickstart.dao.impl.ElementsDaoImpl;
import org.nanoframework.examples.quickstart.domain.Element;

import com.google.inject.ImplementedBy;

/**
 *
 * @author yanghe
 * @since 1.3.5
 */
@ImplementedBy(ElementsDaoImpl.class)
public interface ElementsDao {
    /**
     * 获取所有Element对象.
     * @return All Element
     */
    List<Element> findAll();
    
    /**
     * 根据元素Id获取Element对象.
     * 
     * @param id The Element ID
     * @return Element
     */
    Element findById(long id);
    
    /**
     * 新增Element.
     * 
     * @param el Element
     * @return insert successful
     */
    long insert(Element el);
    
    /**
     * 更新Element.
     * 
     * @param el Element
     * @return update successful
     */
    long update(Element el);
    
    /**
     * 删除Element.
     * 
     * @param id The Element ID
     * @return delete successful
     */
    long deleteById(long id);
    
    /**
     * 批量新增 Element.
     * @param els Element List
     * @return insert successful
     */
    long insertBatch(List<Element> els);
    
}
