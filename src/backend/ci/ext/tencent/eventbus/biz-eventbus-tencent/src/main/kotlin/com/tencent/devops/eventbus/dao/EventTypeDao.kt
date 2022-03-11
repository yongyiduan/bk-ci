/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.eventbus.dao

import com.tencent.devops.common.api.util.timestampmilli
import com.tencent.devops.eventbus.pojo.EventType
import com.tencent.devops.model.eventbus.tables.TEventType
import com.tencent.devops.model.eventbus.tables.records.TEventTypeRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class EventTypeDao {

    fun create(dslContext: DSLContext, eventType: EventType) {
        val now = LocalDateTime.now()
        with(TEventType.T_EVENT_TYPE) {
            dslContext.insertInto(
                this,
                SOURCE_ID,
                NAME,
                ALIAS_NAME,
                DESC,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                eventType.sourceId,
                eventType.name,
                eventType.aliasName,
                eventType.desc,
                now,
                now
            ).execute()
        }
    }

    fun update(dslContext: DSLContext, eventType: EventType)  {
        val now = LocalDateTime.now()
        with(TEventType.T_EVENT_TYPE) {
            dslContext.update(this)
                .set(SOURCE_ID, eventType.sourceId)
                .set(NAME, eventType.name)
                .set(ALIAS_NAME, eventType.aliasName)
                .set(DESC, eventType.desc)
                .set(UPDATE_TIME, now)
                .where(ID.eq(eventType.id))
                .execute()
        }
    }

    fun list(
        dslContext: DSLContext,
        sourceId: Long?,
        name: String?,
        aliasName: String?,
        offset: Int,
        limit: Int
    ): List<EventType> {
        with(TEventType.T_EVENT_TYPE) {
            val conditions = mutableListOf<Condition>()
            if (name != null) {
                conditions.add(NAME.eq(name))
            }
            if (sourceId != null) {
                conditions.add(SOURCE_ID.eq(sourceId))
            }
            if (aliasName != null) {
                conditions.add(ALIAS_NAME.eq(aliasName))
            }

            val where = if (conditions.isNotEmpty()) {
                dslContext.selectFrom(this).where(conditions)
            } else {
                dslContext.selectFrom(this)
            }
            return where.orderBy(CREATE_TIME.desc())
                .limit(offset, limit)
                .fetch()
                .map { convert(it) }
                .toList()
        }
    }

    fun count(
        dslContext: DSLContext,
        sourceId: Long?,
        name: String?,
        aliasName: String?,
    ): Long {
        with(TEventType.T_EVENT_TYPE) {
            val conditions = mutableListOf<Condition>()
            if (name != null) {
                conditions.add(NAME.eq(name))
            }
            if (sourceId != null) {
                conditions.add(SOURCE_ID.eq(sourceId))
            }
            if (aliasName != null) {
                conditions.add(ALIAS_NAME.eq(aliasName))
            }

            val where = if (conditions.isNotEmpty()) {
                dslContext.selectCount().from(this).where(conditions)
            } else {
                dslContext.selectCount().from(this)
            }
            return where.fetchOne(0, Long::class.java)!!
        }
    }

    fun get(
        dslContext: DSLContext,
        id: Long
    ): EventType? {
        val record = with(TEventType.T_EVENT_TYPE) {
            dslContext.selectFrom(this)
                .where(ID.eq(id))
                .fetchOne()
        } ?: return null
        return convert(record)
    }

    fun getByAliasName(
        dslContext: DSLContext,
        aliasName: String
    ) : EventType? {
        val record = with(TEventType.T_EVENT_TYPE) {
            dslContext.selectFrom(this)
                .where(ALIAS_NAME.eq(aliasName))
                .fetchOne()
        } ?: return null
        return convert(record)
    }

    fun convert(record: TEventTypeRecord): EventType {
        return with(record) {
            EventType(
                id = id,
                sourceId = sourceId,
                name = name,
                aliasName = aliasName,
                desc = desc,
                createTime = createTime.timestampmilli(),
                updateTime = updateTime.timestampmilli()
            )
        }
    }
}
