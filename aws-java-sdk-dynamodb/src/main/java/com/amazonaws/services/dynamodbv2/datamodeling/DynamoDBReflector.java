/*
 * Copyright 2011-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.services.dynamodbv2.datamodeling;

import org.apache.http.annotation.GuardedBy;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Reflection assistant for {@link DynamoDBMapper}
 */
public class DynamoDBReflector {
    /*
     * Several caches for performance. Collectively, they can make this class
     * over twice as fast.
     */
    private final Map<Class<?>, Collection<Method>> getterCache = new HashMap<Class<?>, Collection<Method>>();
    private final Map<Class<?>, Method> primaryHashKeyGetterCache = new HashMap<Class<?>, Method>();
    private final Map<Class<?>, Method> primaryRangeKeyGetterCache = new HashMap<Class<?>, Method>();

    /*
     * All caches keyed by a Method use the getter for a particular mapped
     * property
     */
    private final Map<Method, Method> setterCache = new HashMap<Method, Method>();

    @GuardedBy("readWriteLockAttrName")
    private final Map<Method, String> attributeNameCache = new HashMap<Method, String>();

    private final Map<Method, Boolean> versionAttributeGetterCache = new HashMap<Method, Boolean>();
    private final Map<Method, Boolean> autoGeneratedKeyGetterCache = new HashMap<Method, Boolean>();

    private final ReentrantReadWriteLock readWriteLockAttrName = new ReentrantReadWriteLock();
    private final ReadLock readLockAttrName = readWriteLockAttrName.readLock();
    private final WriteLock writeLockAttrName = readWriteLockAttrName.writeLock();

    /**
     * Returns the set of getter methods which are relevant when marshalling or
     * unmarshalling an object.
     */
    public Collection<Method> getRelevantGetters(Class<?> clazz) {
        synchronized (getterCache) {
            if ( !getterCache.containsKey(clazz) ) {
                List<Method> relevantGetters = findRelevantGetters(clazz);
                getterCache.put(clazz, relevantGetters);
            }
            return getterCache.get(clazz);
        }
    }

    static List<Method> findRelevantGetters(Class<?> clazz) {
        List<Method> relevantGetters = new LinkedList<Method>();
        for ( Method m : clazz.getMethods() ) {
            if ( isRelevantGetter(m) ) {
                relevantGetters.add(m);
            }
        }
        return relevantGetters;
    }

    /**
     * Returns whether the method given is a getter method we should serialize /
     * deserialize to the service. The method must begin with "get" or "is",
     * have no arguments, belong to a class that declares its table, and not be
     * marked ignored.
     */
    private static boolean isRelevantGetter(Method m) {
        return (m.getName().startsWith("get") || m.getName().startsWith("is"))
                && m.getParameterTypes().length == 0
                && ! (m.isBridge() || m.isSynthetic())
                && isDocumentType(m.getDeclaringClass())
                && !ReflectionUtils.getterOrFieldHasAnnotation(m, DynamoDBIgnore.class);
    }

    private static boolean isDocumentType(Class<?> clazz) {
        return (clazz.getAnnotation(DynamoDBTable.class) != null)
                || (clazz.getAnnotation(DynamoDBDocument.class) != null);
    }

    /**
     * Returns the annotated {@link DynamoDBRangeKey} getter for the class
     * given, or null if the class doesn't have one.
     */
    public <T> Method getPrimaryRangeKeyGetter(Class<T> clazz) {
        synchronized (primaryRangeKeyGetterCache) {
            if ( !primaryRangeKeyGetterCache.containsKey(clazz) ) {
                Method rangeKeyMethod = null;
                for ( Method method : getRelevantGetters(clazz) ) {
                    if ( method.getParameterTypes().length == 0
                            && ReflectionUtils.getterOrFieldHasAnnotation(method, DynamoDBRangeKey.class)) {
                        rangeKeyMethod = method;
                        break;
                    }
                }
                primaryRangeKeyGetterCache.put(clazz, rangeKeyMethod);
            }
            return primaryRangeKeyGetterCache.get(clazz);
        }
    }

    /**
     * Returns all annotated {@link DynamoDBHashKey} and
     * {@link DynamoDBRangeKey} getters for the class given, throwing an
     * exception if there isn't one.
     *
     * TODO: caching
     */
    public <T> Collection<Method> getPrimaryKeyGetters(Class<T> clazz) {
        List<Method> keyGetters = new LinkedList<Method>();
        for (Method getter : getRelevantGetters(clazz)) {
            if (ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBHashKey.class)
                    || ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBRangeKey.class)) {
                keyGetters.add(getter);
            }
        }

        return keyGetters;
    }


    /**
     * Returns the annotated {@link DynamoDBHashKey} getter for the class given,
     * throwing an exception if there isn't one.
     */
    public <T> Method getPrimaryHashKeyGetter(Class<T> clazz) {
        Method hashKeyMethod;
        synchronized (primaryHashKeyGetterCache) {
            if ( !primaryHashKeyGetterCache.containsKey(clazz) ) {
                for ( Method method : getRelevantGetters(clazz) ) {
                    if ( method.getParameterTypes().length == 0
                            && ReflectionUtils.getterOrFieldHasAnnotation(method, DynamoDBHashKey.class)) {
                        primaryHashKeyGetterCache.put(clazz, method);
                        break;
                    }
                }
            }
            hashKeyMethod = primaryHashKeyGetterCache.get(clazz);
        }

        if ( hashKeyMethod == null ) {
            throw new DynamoDBMappingException("Public, zero-parameter hash key property must be annotated with "
                    + DynamoDBHashKey.class);
        }
        return hashKeyMethod;
    }

    /**
     * Returns the {@link DynamoDBTable} annotation of the class given, throwing
     * a runtime exception if it isn't annotated.
     */
    <T> DynamoDBTable getTable(Class<T> clazz) {
        DynamoDBTable table = clazz.getAnnotation(DynamoDBTable.class);
        if ( table == null )
            throw new DynamoDBMappingException("Class " + clazz + " must be annotated with " + DynamoDBTable.class);
        return table;
    }

    /**
     * Returns the attribute name corresponding to the given getter method.
     */
    public String getAttributeName(Method getter) {
        String attributeName;
        readLockAttrName.lock();
        try {
            attributeName = attributeNameCache.get(getter);
        } finally {
            readLockAttrName.unlock();
        }
        if ( attributeName != null )
            return attributeName;
        DynamoDBHashKey hashKeyAnnotation = ReflectionUtils.getAnnotationFromGetterOrField(getter, DynamoDBHashKey.class);
        if ( hashKeyAnnotation != null ) {
            attributeName = hashKeyAnnotation.attributeName();
            if ( attributeName != null && attributeName.length() > 0 )
                return cacheAttributeName(getter, attributeName);
        }
        DynamoDBIndexHashKey indexHashKey = ReflectionUtils.getAnnotationFromGetterOrField(getter, DynamoDBIndexHashKey.class);
        if ( indexHashKey != null ) {
            attributeName = indexHashKey.attributeName();
            if ( attributeName != null && attributeName.length() > 0 )
                return cacheAttributeName(getter, attributeName);
        }
        DynamoDBRangeKey rangeKey = ReflectionUtils.getAnnotationFromGetterOrField(getter, DynamoDBRangeKey.class);
        if ( rangeKey != null ) {
            attributeName = rangeKey.attributeName();
            if ( attributeName != null && attributeName.length() > 0 )
                return cacheAttributeName(getter, attributeName);
        }
        DynamoDBIndexRangeKey indexRangeKey = ReflectionUtils.getAnnotationFromGetterOrField(getter, DynamoDBIndexRangeKey.class);
        if ( indexRangeKey != null ) {
            attributeName = indexRangeKey.attributeName();
            if ( attributeName != null && attributeName.length() > 0 )
                return cacheAttributeName(getter, attributeName);
        }
        DynamoDBAttribute attribute = ReflectionUtils.getAnnotationFromGetterOrField(getter, DynamoDBAttribute.class);
        if ( attribute != null ) {
            attributeName = attribute.attributeName();
            if ( attributeName != null && attributeName.length() > 0 )
                return cacheAttributeName(getter, attributeName);
        }
        DynamoDBVersionAttribute version = ReflectionUtils.getAnnotationFromGetterOrField(getter, DynamoDBVersionAttribute.class);
        if ( version != null ) {
            attributeName = version.attributeName();
            if ( attributeName != null && attributeName.length() > 0 )
                return cacheAttributeName(getter, attributeName);
        }
        // Default to the camel-cased field name of the getter method, inferred
        // according to the Java naming convention.
        attributeName = ReflectionUtils.getFieldNameByGetter(getter, true);
        return cacheAttributeName(getter, attributeName);
    }

    private String cacheAttributeName(Method getter, String attributeName) {
        writeLockAttrName.lock();
        try {
            attributeNameCache.put(getter, attributeName);
        } finally {
            writeLockAttrName.unlock();
        }
        return attributeName;
    }

    /**
     * Returns the setter corresponding to the getter given, or null if no such
     * setter exists.
     */
    public Method getSetter(Method getter) {
        synchronized (setterCache) {
            if ( !setterCache.containsKey(getter) ) {
                String fieldName = ReflectionUtils.getFieldNameByGetter(getter, false);
                String setterName = "set" + fieldName;
                Method setter = null;
                try {
                    setter = getter.getDeclaringClass().getMethod(setterName, getter.getReturnType());
                } catch ( NoSuchMethodException e ) {
                    throw new DynamoDBMappingException("Expected a public, one-argument method called " + setterName
                            + " on class " + getter.getDeclaringClass(), e);
                } catch ( SecurityException e ) {
                    throw new DynamoDBMappingException("No access to public, one-argument method called " + setterName
                            + " on class " + getter.getDeclaringClass(), e);
                }
                setterCache.put(getter, setter);
            }
            return setterCache.get(getter);
        }
    }

    /**
     * Returns whether the method given is an annotated, no-args getter of a
     * version attribute.
     */
    public boolean isVersionAttributeGetter(Method getter) {
        synchronized (versionAttributeGetterCache) {
            if ( !versionAttributeGetterCache.containsKey(getter) ) {
                versionAttributeGetterCache.put(
                        getter,
                        getter.getName().startsWith("get") && getter.getParameterTypes().length == 0
                                && ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBVersionAttribute.class));
            }
            return versionAttributeGetterCache.get(getter);
        }
    }

    /**
     * Returns whether the method given is an assignable key getter.
     */
    public boolean isAssignableKey(Method getter) {
        synchronized (autoGeneratedKeyGetterCache) {
            if ( !autoGeneratedKeyGetterCache.containsKey(getter) ) {
                autoGeneratedKeyGetterCache.put(
                        getter,
                        ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBAutoGeneratedKey.class)
                                && ( ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBHashKey.class) ||
                                     ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBRangeKey.class) ||
                                     ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBIndexHashKey.class) ||
                                     ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBIndexRangeKey.class)));
            }
            return autoGeneratedKeyGetterCache.get(getter);
        }
    }

    /**
     * Returns the name of the primary hash key.
     */
    public String getPrimaryHashKeyName(Class<?> clazz) {
        return getAttributeName(getPrimaryHashKeyGetter(clazz));
    }

    /**
     * Returns the name of the primary range key, or null if the table does not
     * one.
     */
    public String getPrimaryRangeKeyName(Class<?> clazz) {
        Method primaryRangeKeyGetter = getPrimaryHashKeyGetter(clazz);
        return primaryRangeKeyGetter == null ?
                null
                :
                getAttributeName(getPrimaryRangeKeyGetter(clazz));
    }

    /**
     * Returns true if and only if the specified class has declared a
     * primary range key.
     */
    public boolean hasPrimaryRangeKey(Class<?> clazz) {
        return getPrimaryRangeKeyGetter(clazz) != null;
    }
}
