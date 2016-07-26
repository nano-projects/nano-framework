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
package org.nanoframework.orm.jdbc.domain;

import java.sql.Timestamp;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.orm.jdbc.record.annotation.Column;
import org.nanoframework.orm.jdbc.record.annotation.Id;
import org.nanoframework.orm.jdbc.record.annotation.Table;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
@Table("users")
public class User extends BaseEntity {
    private static final long serialVersionUID = 81442104623246630L;

    @Id
    @Column("id")
    private Long id;
    
    @Column("username")
    private String username;
    
    @Column("password")
    private String password;
    
    @Column("password_salt")
    private String passwordSalt;
    
    @Column("email")
    private String email;
    
    @Column("status")
    private Integer status;
    
    @Column("locked")
    private Integer locked;
    
    @Column("activate")
    private Integer activate;
    
    @Column("create_time")
    private Timestamp createTime;
    
    @Column("modify_time")
    private Timestamp modifyTime;
    
    @Column("deleted")
    private Integer deleted;

    /**
     * 
     *
     * @author yanghe
     * @since 0.0.1
     */
    public enum Type {
        /** User active status type. */
        ACTIVE,
        /** User admin status type. */
        ADMIN,
        /** User blocked status type. */
        BLOCKED
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return the passwordSalt
     */
    public String getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * @param passwordSalt the passwordSalt to set
     */
    public void setPasswordSalt(final String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final Integer status) {
        this.status = status;
    }

    /**
     * @return the locked
     */
    public Integer getLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(final Integer locked) {
        this.locked = locked;
    }

    /**
     * @return the activate
     */
    public Integer getActivate() {
        return activate;
    }

    /**
     * @param activate the activate to set
     */
    public void setActivate(final Integer activate) {
        this.activate = activate;
    }

    /**
     * @return the createTime
     */
    public Timestamp getCreateTime() {
        return createTime == null ? null : new Timestamp(createTime.getTime());
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(final Timestamp createTime) {
        if (createTime != null) {
            if (this.createTime == null) {
                this.createTime = new Timestamp(createTime.getTime());
            } else {
                this.createTime.setTime(createTime.getTime());
            }
        } else {
            this.createTime = null;
        }
    }

    /**
     * @return the modifyTime
     */
    public Timestamp getModifyTime() {
        return modifyTime == null ? null : new Timestamp(modifyTime.getTime());
    }

    /**
     * @param modifyTime the modifyTime to set
     */
    public void setModifyTime(final Timestamp modifyTime) {
        if (modifyTime != null) {
            if (this.modifyTime == null) {
                this.modifyTime = new Timestamp(modifyTime.getTime());
            } else {
                this.modifyTime.setTime(modifyTime.getTime());
            }
        } else {
            this.modifyTime = null;
        }
    }

    /**
     * @return the deleted
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(final Integer deleted) {
        this.deleted = deleted;
    }

}
