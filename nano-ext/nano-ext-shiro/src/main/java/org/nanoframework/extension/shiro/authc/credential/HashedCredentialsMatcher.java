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
package org.nanoframework.extension.shiro.authc.credential;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.util.StringUtils;
import org.nanoframework.extension.shiro.authc.SaltedAuthenticationInfo;
import org.nanoframework.extension.shiro.crypto.hash.AbstractHash;
import org.nanoframework.extension.shiro.crypto.hash.Hash;
import org.nanoframework.extension.shiro.crypto.hash.SimpleHash;

/**
 * 
 * @since 0.9
 */
public class HashedCredentialsMatcher extends SimpleCredentialsMatcher {

    /**
     * @since 1.1
     */
    private String hashAlgorithm;
    private int hashIterations;
    private boolean hashSalted;
    private boolean storedCredentialsHexEncoded;

    /**
     * JavaBeans-compatibile no-arg constructor intended for use in IoC/Dependency Injection environments.  If you
     * use this constructor, you <em>MUST</em> also additionally set the
     * {@link #setHashAlgorithmName(String) hashAlgorithmName} property.
     */
    public HashedCredentialsMatcher() {
        this.hashAlgorithm = null;
        this.hashSalted = false;
        this.hashIterations = 1;
        this.storedCredentialsHexEncoded = true; //false means Base64-encoded
    }

    /**
     * Creates an instance using the specified {@link #getHashAlgorithmName() hashAlgorithmName} to hash submitted
     * credentials.
     * @param hashAlgorithmName the {@code Hash} {@link org.apache.shiro.crypto.hash.Hash#getAlgorithmName() algorithmName}
     *                          to use when performing hashes for credentials matching.
     * @since 1.1
     */
    public HashedCredentialsMatcher(String hashAlgorithmName) {
        this();
        if (!StringUtils.hasText(hashAlgorithmName) ) {
            throw new IllegalArgumentException("hashAlgorithmName cannot be null or empty.");
        }
        this.hashAlgorithm = hashAlgorithmName;
    }

    /**
     * Returns the {@code Hash} {@link org.apache.shiro.crypto.hash.Hash#getAlgorithmName() algorithmName} to use
     * when performing hashes for credentials matching.
     *
     * @return the {@code Hash} {@link org.apache.shiro.crypto.hash.Hash#getAlgorithmName() algorithmName} to use
     *         when performing hashes for credentials matching.
     * @since 1.1
     */
    public String getHashAlgorithmName() {
        return hashAlgorithm;
    }

    /**
     * Sets the {@code Hash} {@link org.apache.shiro.crypto.hash.Hash#getAlgorithmName() algorithmName} to use
     * when performing hashes for credentials matching.
     *
     * @param hashAlgorithmName the {@code Hash} {@link org.apache.shiro.crypto.hash.Hash#getAlgorithmName() algorithmName}
     *                          to use when performing hashes for credentials matching.
     * @since 1.1
     */
    public void setHashAlgorithmName(String hashAlgorithmName) {
        this.hashAlgorithm = hashAlgorithmName;
    }

    /**
     *
     * @return {@code true} if the system's stored credential hash is Hex encoded, {@code false} if it
     *         is Base64 encoded.  Default is {@code true}
     */
    public boolean isStoredCredentialsHexEncoded() {
        return storedCredentialsHexEncoded;
    }

    /**
     * @param storedCredentialsHexEncoded the indicator if this system's stored credential hash is Hex
     *                                    encoded or not ('not' automatically implying it is Base64 encoded).
     */
    public void setStoredCredentialsHexEncoded(boolean storedCredentialsHexEncoded) {
        this.storedCredentialsHexEncoded = storedCredentialsHexEncoded;
    }

    /**
     *
     * @return {@code true} if a submitted {@code AuthenticationToken}'s credentials should be salted when hashing,
     *         {@code false} if it should not be salted.
     */
    @Deprecated
    public boolean isHashSalted() {
        return hashSalted;
    }

    /**
     *
     * @param hashSalted whether or not to salt a submitted {@code AuthenticationToken}'s credentials when hashing.
     */
    @Deprecated
    public void setHashSalted(boolean hashSalted) {
        this.hashSalted = hashSalted;
    }

    /**
     *
     * @return the number of times a submitted {@code AuthenticationToken}'s credentials will be hashed before
     *         comparing to the credentials stored in the system.
     */
    public int getHashIterations() {
        return hashIterations;
    }

    /**
     *
     * @param hashIterations the number of times to hash a submitted {@code AuthenticationToken}'s credentials.
     */
    public void setHashIterations(int hashIterations) {
        if (hashIterations < 1) {
            this.hashIterations = 1;
        } else {
            this.hashIterations = hashIterations;
        }
    }

    /**
     *
     * @param token the AuthenticationToken submitted during the authentication attempt.
     * @return a salt value to use to hash the authentication token's credentials.
     */
    @Deprecated
    protected Object getSalt(AuthenticationToken token) {
        return token.getPrincipal();
    }

    /**
     *
     * @param info the AuthenticationInfo from which to retrieve the credentials which assumed to be in already-hashed form.
     * @return a {@link Hash Hash} instance representing the given AuthenticationInfo's stored credentials.
     */
    protected Object getCredentials(AuthenticationInfo info) {
        Object credentials = info.getCredentials();

        byte[] storedBytes = toBytes(credentials);

        if (credentials instanceof String || credentials instanceof char[]) {
            //account.credentials were a char[] or String, so
            //we need to do text decoding first:
            if (isStoredCredentialsHexEncoded()) {
                storedBytes = Hex.decode(storedBytes);
            } else {
                storedBytes = Base64.decode(storedBytes);
            }
        }
        AbstractHash hash = newHashInstance();
        hash.setBytes(storedBytes);
        return hash;
    }

    /**
     *
     * @param token the {@code AuthenticationToken} submitted during the authentication attempt.
     * @param info  the {@code AuthenticationInfo} stored in the system matching the token principal
     * @return {@code true} if the provided token credentials hash match to the stored account credentials hash,
     *         {@code false} otherwise
     * @since 1.1
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        Object tokenHashedCredentials = hashProvidedCredentials(token, info);
        Object accountCredentials = getCredentials(info);
        return equals(tokenHashedCredentials, accountCredentials);
    }

    /**
     *
     * @param token the submitted authentication token from which its credentials will be hashed
     * @param info  the stored account data, potentially used to acquire a salt
     * @return the token credentials hash
     * @since 1.1
     */
    protected Object hashProvidedCredentials(AuthenticationToken token, AuthenticationInfo info) {
        Object salt = null;
        if (info instanceof SaltedAuthenticationInfo) {
            salt = ((SaltedAuthenticationInfo) info).getCredentialsSalt();
        } else {
            //retain 1.0 backwards compatibility:
            if (isHashSalted()) {
                salt = getSalt(token);
            }
        }
        return hashProvidedCredentials(token.getCredentials(), salt, getHashIterations());
    }

    /**
     * Returns the {@link #getHashAlgorithmName() hashAlgorithmName} property, but will throw an
     * {@link IllegalStateException} if it has not been set.
     *
     * @return the required {@link #getHashAlgorithmName() hashAlgorithmName} property
     * @throws IllegalStateException if the property has not been set prior to calling this method.
     * @since 1.1
     */
    private String assertHashAlgorithmName() throws IllegalStateException {
        String hashAlgorithmName = getHashAlgorithmName();
        if (hashAlgorithmName == null) {
            String msg = "Required 'hashAlgorithmName' property has not been set.  This is required to execute " +
                    "the hashing algorithm.";
            throw new IllegalStateException(msg);
        }
        return hashAlgorithmName;
    }

    /**
     * Hashes the provided credentials a total of {@code hashIterations} times, using the given salt.  The hash
     * implementation/algorithm used is based on the {@link #getHashAlgorithmName() hashAlgorithmName} property.
     *
     * @param credentials    the submitted authentication token's credentials to hash
     * @param salt           the value to salt the hash, or {@code null} if a salt will not be used.
     * @param hashIterations the number of times to hash the credentials.  At least one hash will always occur though,
     *                       even if this argument is 0 or negative.
     * @return the hashed value of the provided credentials, according to the specified salt and hash iterations.
     */
    protected Hash hashProvidedCredentials(Object credentials, Object salt, int hashIterations) {
        String hashAlgorithmName = assertHashAlgorithmName();
        return new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations);
    }

    /**
     * Returns a new, <em>uninitialized</em> instance, without its byte array set.  Used as a utility method in the
     * {@link SimpleCredentialsMatcher#getCredentials(org.apache.shiro.authc.AuthenticationInfo) getCredentials(AuthenticationInfo)} implementation.
     *
     * @return a new, <em>uninitialized</em> instance, without its byte array set.
     */
    protected AbstractHash newHashInstance() {
        String hashAlgorithmName = assertHashAlgorithmName();
        return new SimpleHash(hashAlgorithmName);
    }

}
