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
package org.nanoframework.concurrent.scheduler.defaults.monitor;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class Pointer extends BaseEntity {
    private static final long serialVersionUID = 7350989467756362845L;

    private String scene;
    private Long time;
    private Long tps;

    public Pointer() {

    }

    private Pointer(String scene, long time, long tps) {
        this.scene = scene;
        this.time = time;
        this.tps = tps;
    }

    public static final Pointer create(String scene, long time, long tps) {
        return new Pointer(scene, time, tps);
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTps() {
        return tps;
    }

    public void setTps(Long tps) {
        this.tps = tps;
    }

}
