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
package org.nanoframework.examples.quickstart.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.examples.quickstart.dao.ElementsDao;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.orm.jdbc.JdbcAdapter;

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.3.5
 */
@SuppressWarnings("unchecked")
public class ElementsDaoImpl implements ElementsDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementsDaoImpl.class);
    private static final String FIND_ALL = "select id, text from elements";
    private static final String FIND_BY_ID = FIND_ALL + " where id = ?";
    private static final String INSERT = "insert into elements (text) values (?)";
    private static final String UPDATE = "update elements set text = ? where id = ?";
    private static final String DELETE_BY_ID = "delete from elements where id = ?";
    
    private final JdbcAdapter adapter = JdbcAdapter.ADAPTER;
    
    @Override
    public List<Element> findAll() {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return Element._getMapToBeans(Arrays.<Map<String, Object>>asList(adapter.executeQuery(FIND_ALL, conn).getRows()), Element.class);
        } catch(SQLException e) {
            LOGGER.error("Find ALL Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return Collections.emptyList();
    }

    @Override
    public Element findById(long id) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            SortedMap<String, Object>[] result = adapter.executeQuery(FIND_BY_ID, Lists.newArrayList(id), conn).getRows();
            if(!ArrayUtils.isEmpty(result)) {
                return Element._getMapToBean(result[0], Element.class);
            }
            
        } catch(SQLException e) {
            LOGGER.error("Find Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return null;
    }

    @Override
    public long insert(Element el) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return adapter.executeUpdate(INSERT, Lists.newArrayList(el.getText()), conn);
        } catch(SQLException e) {
            LOGGER.error("Insert Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return 0;
    }
    
    @Override
    public long update(Element el) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return adapter.executeUpdate(UPDATE, Lists.newArrayList(el.getText(), el.getId()), conn);
        } catch(SQLException e) {
            LOGGER.error("Update Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return 0;
    }

    @Override
    public long deleteById(long id) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return adapter.executeUpdate(DELETE_BY_ID, Lists.newArrayList(id), conn);
        } catch(SQLException e) {
            LOGGER.error("Delete Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return 0;
    }

    @Override
    public long insertBatch(List<Element> els) {
        Assert.notEmpty(els, "els list must be not empty");
        long successful = 0;
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            conn.setAutoCommit(false);
            for(Element el : els) {
                successful += adapter.executeUpdate(INSERT, Lists.newArrayList(el.getText()), conn);
            }
            
            adapter.commit(conn);
        } catch(SQLException e) {
            LOGGER.error("Insert Element error: {}", e.getMessage());
            try {
                adapter.rollback(conn);
            } catch (SQLException e1) {
                LOGGER.error("Rollback connection error: {}", e1.getMessage());
            }
            
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            adapter.close(conn);
        }
        
        return successful;
    }
}
