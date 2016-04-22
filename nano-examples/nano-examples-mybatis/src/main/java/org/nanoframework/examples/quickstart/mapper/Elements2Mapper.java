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
package org.nanoframework.examples.quickstart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.nanoframework.examples.quickstart.domain.Element;

/**
 *
 * @author yanghe
 * @since 1.3.5
 */
public interface Elements2Mapper {
    /**
     * 获取所有Element对象.
     * @return All Element
     */
    @Select("select id, text from elements")
    List<Element> findAll();
    
    /**
     * 根据元素Id获取Element对象.
     * 
     * @param id The Element ID
     * @return Element
     */
    @Select("select id, text from elements where id = #{id}")
    Element findById(@Param("id") long id);
    
    /**
     * 新增Element.
     * 
     * @param el Element
     * @return insert successful
     */
    @Insert("insert into elements (text) values (#{text})")
    long insert(Element el);
    
    /**
     * 更新Element.
     * 
     * @param el Element
     * @return update successful
     */
    @Update("update elements set text = #{text} where id = #{id}")
    long update(Element el);
    
    /**
     * 删除Element.
     * 
     * @param id The Element ID
     * @return delete successful
     */
    @Delete("delete from elements where id = #{id}")
    long deleteById(@Param("id") long id);
    
}
