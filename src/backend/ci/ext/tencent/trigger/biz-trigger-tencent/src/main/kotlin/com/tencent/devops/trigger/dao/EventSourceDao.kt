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

package com.tencent.devops.trigger.dao

import com.tencent.devops.common.api.util.timestampmilli
import com.tencent.devops.trigger.pojo.EventSource
import com.tencent.devops.model.trigger.tables.TEventSource
import com.tencent.devops.model.trigger.tables.records.TEventSourceRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class EventSourceDao {

    fun create(dslContext: DSLContext, eventSource: EventSource) {
        val now = LocalDateTime.now()
        with(TEventSource.T_EVENT_SOURCE) {
            dslContext.insertInto(
                this,
                NAME,
                DESC,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                eventSource.name,
                eventSource.desc,
                now,
                now
            ).execute()
        }
    }

    fun update(dslContext: DSLContext, eventSource: EventSource) {
        val now = LocalDateTime.now()
        with(TEventSource.T_EVENT_SOURCE) {
            dslContext.update(this)
                .set(NAME, eventSource.name)
                .set(DESC, eventSource.desc)
                .set(UPDATE_TIME, now)
                .where(ID.eq(eventSource.id))
                .execute()
        }
    }

    fun list(
        dslContext: DSLContext,
        name: String?,
        offset: Int,
        limit: Int
    ): List<EventSource> {
        with(TEventSource.T_EVENT_SOURCE) {
            val where = if (name != null) {
                dslContext.selectFrom(this).where(NAME.eq(name))
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
        name: String?
    ): Long {
        with(TEventSource.T_EVENT_SOURCE) {
            val where = if (name != null) {
                dslContext.selectCount().from(this).where(NAME.eq(name))
            } else {
                dslContext.selectFrom(this)
            }
            return where.fetchOne(0, Long::class.java)!!
        }
    }

    fun get(
        dslContext: DSLContext,
        id: Long
    ): EventSource? {
        val record = with(TEventSource.T_EVENT_SOURCE) {
            dslContext.selectFrom(this)
                .where(ID.eq(id))
                .fetchOne()
        } ?: return null
        return convert(record)
    }

    fun convert(record: TEventSourceRecord): EventSource {
        return with(record) {
            EventSource(
                id = id,
                name = name,
                desc = desc,
                createTime = createTime.timestampmilli(),
                updateTime = updateTime.timestampmilli()
            )
        }
    }
}
