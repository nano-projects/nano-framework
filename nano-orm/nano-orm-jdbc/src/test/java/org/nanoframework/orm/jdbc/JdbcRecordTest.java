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
package org.nanoframework.orm.jdbc;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.ResourceUtils;
import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;
import org.nanoframework.orm.jdbc.binding.JdbcManager;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.domain.User;
import org.nanoframework.orm.jdbc.jstl.Result;
import org.nanoframework.orm.jdbc.record.JdbcRecord;
import org.nanoframework.orm.jdbc.record.Record;
import org.nanoframework.orm.jdbc.record.exception.MultiRecordException;
import org.nanoframework.orm.jdbc.record.script.SQLScript;

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class JdbcRecordTest {
    private static final String TEST = "test";
    private Record<User> record;
    
    @Before
    public void init() throws FileNotFoundException, SQLException {
        final Properties prop = PropertiesLoader.load("/jdbc-test.properties");
        if (prop != null) {
            JdbcAdapter.newInstance(Lists.newArrayList(new DruidJdbcConfig(prop)), PoolType.DRUID, this.getClass());
        }

        System.setProperty(Result.JDBC_JSTL_CASE_INSENSITIVE_ORDER_PROPERTY, "true");
        final JdbcManager manager = GlobalJdbcManager.get(TEST);
        manager.execute("drop all objects");
        manager.execute("runscript from '" + ResourceUtils.getURL("classpath:test-schema.sql") + '\'');
        
        record = new JdbcRecord<User>(TEST) {
        };
    }

    @Test
    public void selectTest() throws SQLException {
        final List<User> users = record.select(new String[] { "username", "email" }, "admin", "admin@example.com");
        Assert.assertTrue(!users.isEmpty());
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getUsername(), "admin");
    }

    @Test
    public void selectOneTest() throws SQLException {
        try {
            record.selectOne("select * from users");
        } catch (final Throwable e) {
            Assert.assertTrue(e instanceof MultiRecordException);
        }

        final User user = record.selectOne("select * from users where username = ?", "admin");
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUsername(), "admin");
    }
    
    @Test
    public void selectAllTest() throws SQLException {
        final List<User> users = record.select("select username, email from users");
        Assert.assertTrue(!users.isEmpty());
        Assert.assertEquals(users.size(), 2);
        final User user = users.get(0);
        Assert.assertEquals(user.getUsername(), "admin");
        Assert.assertEquals(user.getEmail(), "admin@example.com");
    }
    
    @Test
    public void selectWhereTest() throws SQLException {
        final List<User> users = record.select("select username, email from users where username = ?", "admin");
        Assert.assertTrue(!users.isEmpty());
        Assert.assertEquals(users.size(), 1);
        final User user = users.get(0);
        Assert.assertEquals(user.getUsername(), "admin");
        Assert.assertEquals(user.getEmail(), "admin@example.com");
    }

    @Test
    public void selectAllSimpleTest() throws SQLException {
        final List<User> users = record.select();
        Assert.assertTrue(!users.isEmpty());
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0).getUsername(), "admin");
    }

    @Test
    public void selectSimpleTest() throws SQLException {
        final List<User> users = record.select(new String[] { "username", "email" }, new String[] { "username" }, "admin");
        Assert.assertTrue(!users.isEmpty());
        Assert.assertEquals(users.size(), 1);

        final User user = users.get(0);
        Assert.assertEquals(user.getUsername(), "admin");
        Assert.assertEquals(user.getEmail(), "admin@example.com");
    }

    @Test
    public void selectOneSimpleTest() throws SQLException {
        final User user = record.selectOne(new String[] { "username" }, "admin");
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUsername(), "admin");
        Assert.assertEquals(user.getEmail(), "admin@example.com");
    }
    
    @Test
    public void selectOneSimple2Test() throws SQLException {
        final User user = record.selectOne(new String[] { "username", "email"}, new String[] { "id" }, 1);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUsername(), "admin");
        Assert.assertEquals(user.getEmail(), "admin@example.com");
        Assert.assertTrue(user.getId() == null);
    }
    
    @Test
    public void selectNotFoundTest() throws SQLException {
        final User user = record.selectOne(new String[] { "username" }, "notfound");
        Assert.assertTrue(user == null);
    }
    
    @Test
    public void insertAndUpdateTest() throws SQLException {
        final User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        user.setPasswordSalt("123");
        user.setEmail("123");
        Assert.assertTrue(record.insert(user) > 0);

        final User newUser = record.selectOne("select * from users where username = ?", "test");
        newUser.setPassword("345");
        newUser.setPasswordSalt("456");
        newUser.setEmail("567");
        Assert.assertTrue(record.update(newUser, Lists.newArrayList("password", "passwordSalt", "email"), Lists.newArrayList("username")) > 0);

        Assert.assertTrue(record.delete(newUser) > 0);
    }
    
    @Test
    public void insertBatchTest() throws SQLException {
        final List<User> users = Lists.newArrayList();
        for (int idx = 0; idx < 5; idx++) {
            final String idxStr = String.valueOf(idx);
            final User user = new User();
            user.setUsername("user" + idxStr);
            user.setPassword(idxStr);
            user.setPasswordSalt(idxStr);
            user.setEmail(idxStr);
            users.add(user);
        }
        
        final int[] rets = record.insertBatch(users);
        Assert.assertEquals(rets.length, 5);
        final AtomicInteger total = new AtomicInteger(0);
        for (int ret : rets) {
            total.addAndGet(ret);
        }
        
        Assert.assertEquals(total.get(), 5);
        
        final List<User> delUsers = record.select("select * from users where username like 'user%'");
        
        int[] dels = record.deleteBatch(delUsers);
        Assert.assertEquals(dels.length, 5);
        final AtomicInteger delTotal = new AtomicInteger(0);
        for (int del : dels) {
            delTotal.addAndGet(del);
        }
        
        Assert.assertEquals(delTotal.get(), 5);
    }
    
    @Test
    public void updateTest() throws SQLException {
        final List<String> where = Lists.newArrayList("username");
        final User user = record.selectOne(where, "admin");
        user.setDeleted(1);
        Assert.assertEquals(record.update(user), 1);
        
        final User newUser = record.selectOne(where, "admin");
        Assert.assertEquals(newUser.getDeleted().intValue(), 1);
    }
    
    @Test
    public void update2Test() throws SQLException {
        final User user = record.selectOne(Lists.newArrayList("id"), 1);
        Assert.assertNotNull(user);
        user.setUsername("testname");
        Assert.assertEquals(record.update(user, new String[] { "username" }, new String[] { "id" }), 1);
        
        final User newUser = record.selectOne(Lists.newArrayList("username"), Lists.newArrayList("id"), 1);
        Assert.assertNotNull(newUser);
        Assert.assertEquals(newUser.getUsername(), "testname");
    }
    
    @Test
    public void updateByScriptTest() throws SQLException {
        Assert.assertEquals(record.update(SQLScript.create("update users set username = ? where username = ?", Lists.newArrayList("testname", "admin"))), 1);
        final User user = record.selectOne(Lists.newArrayList("id"), 1);
        Assert.assertEquals(user.getUsername(), "testname");
    }
    
    @Test
    public void updateBatch0Test() throws SQLException {
        final int[] rets = record.updateBatch((List<User>) null);
        Assert.assertEquals(rets.length, 0);
        
        int[] rets2 = record.updateBatch(Collections.emptyList());
        Assert.assertEquals(rets2.length, 0);
    }
    
    @Test
    public void updateBatch1Test() throws SQLException {
        final List<User> users = record.select();
        Assert.assertEquals(users.size(), 2);
        
        users.forEach(user -> user.setDeleted(1));
        final int[] rets = record.updateBatch(users);
        Assert.assertEquals(rets.length, 2);
        final AtomicInteger total = new AtomicInteger(0);
        for (int idx = 0, len = rets.length; idx < len; idx++) {
            total.addAndGet(rets[idx]);
        }
        
        Assert.assertEquals(total.get(), 2);
        
        final List<User> deletedUsers = record.select();
        for (User user : deletedUsers) {
            Assert.assertEquals(user.getDeleted().intValue(), 1);
        }
    }
    
    @Test
    public void updateBatch2Test() throws SQLException {
        final List<User> users = record.select();
        Assert.assertEquals(users.size(), 2);
        
        users.forEach(user -> user.setDeleted(1));
        final int[] rets = record.updateBatch(users, new String[] { "deleted" }, new String[] { "id" });
        Assert.assertEquals(rets.length, 2);
        final AtomicInteger total = new AtomicInteger(0);
        for (int idx = 0, len = rets.length; idx < len; idx++) {
            total.addAndGet(rets[idx]);
        }
        
        Assert.assertEquals(total.get(), 2);
        
        final List<User> deletedUsers = record.select();
        for (User user : deletedUsers) {
            Assert.assertEquals(user.getDeleted().intValue(), 1);
        }
    }

    @After
    public void destroy() {
        JdbcAdapter.adapter().shutdown();
    }
}
