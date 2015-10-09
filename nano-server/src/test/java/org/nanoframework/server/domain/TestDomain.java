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
package org.nanoframework.server.domain;

import java.sql.Timestamp;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:15:58
 */
public class TestDomain extends BaseEntity {
	private String waybillNo;
	private String sourceOrgCode;
	private String desOrgCode;
	private String sourceOrgCityCode;
	private Timestamp createTime;

	public static final String WAYBILL_NO = "waybillNo";
	public static final String SOURCE_ORG_CODE = "sourceOrgCode";
	public static final String DES_ORG_CODE = "desOrgCode";
	public static final String SOURCE_ORG_CITY_CODE = "sourceOrgCityCode";
	public static final String CREATE_TIME = "createTime";

	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	public String getSourceOrgCode() {
		return sourceOrgCode;
	}

	public void setSourceOrgCode(String sourceOrgCode) {
		this.sourceOrgCode = sourceOrgCode;
	}

	public String getDesOrgCode() {
		return desOrgCode;
	}

	public void setDesOrgCode(String desOrgCode) {
		this.desOrgCode = desOrgCode;
	}

	public String getSourceOrgCityCode() {
		return sourceOrgCityCode;
	}

	public void setSourceOrgCityCode(String sourceOrgCityCode) {
		this.sourceOrgCityCode = sourceOrgCityCode;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

}
