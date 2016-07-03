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
package org.nanoframework.commons.support.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Handles messages that contain an Object.
 * @author yanghe
 * @since 1.4.0
 */
public class ObjectMessage implements Message {

    private static final long serialVersionUID = -5903272448334166185L;

    private transient Object obj;
    private transient String objectString;

    /**
     * Creates the ObjectMessage.
     * 
     * @param obj The Object to format.
     */
    public ObjectMessage(final Object obj) {
        this.obj = obj == null ? "null" : obj;
    }

    /**
     * Returns the formatted object message.
     * 
     * @return the formatted object message.
     */
    @Override
    public String getFormattedMessage() {
        // LOG4J2-763: cache formatted string in case obj changes later
        if (objectString == null) {
            objectString = String.valueOf(obj);
        }
        return objectString;
    }

    /**
     * Returns the object formatted using its toString method.
     * 
     * @return the String representation of the object.
     */
    @Override
    public String getFormat() {
        return getFormattedMessage();
    }

    /**
     * Returns the object as if it were a parameter.
     * 
     * @return The object.
     */
    @Override
    public Object[] getParameters() {
        return new Object[] { obj };
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ObjectMessage that = (ObjectMessage) o;
        return obj == null ? that.obj == null : equalObjectsOrStrings(obj, that.obj);
    }

    private boolean equalObjectsOrStrings(final Object left, final Object right) {
        return left.equals(right) || String.valueOf(left).equals(String.valueOf(right));
    }

    @Override
    public int hashCode() {
        return obj != null ? obj.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ObjectMessage[obj=" + getFormattedMessage() + ']';
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (obj instanceof Serializable) {
            out.writeObject(obj);
        } else {
            out.writeObject(String.valueOf(obj));
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        obj = in.readObject();
    }

    /**
     * Gets the message if it is a throwable.
     *
     * @return the message if it is a throwable.
     */
    @Override
    public Throwable getThrowable() {
        return obj instanceof Throwable ? (Throwable) obj : null;
    }
}
