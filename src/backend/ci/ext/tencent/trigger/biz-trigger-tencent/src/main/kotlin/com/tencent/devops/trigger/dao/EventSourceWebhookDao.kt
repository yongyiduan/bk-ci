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
import com.tencent.devops.trigger.pojo.EventSourceWebhook
import com.tencent.devops.model.trigger.tables.TEventSourceWebhook
import com.tencent.devops.model.trigger.tables.records.TEventSourceWebhookRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class EventSourceWebhookDao {

    fun create(dslContext: DSLContext, eventSourceWebhook: EventSourceWebhook) {
        val now = LocalDateTime.now()
        with(TEventSourceWebhook.T_EVENT_SOURCE_WEBHOOK) {
            dslContext.insertInto(
                this,
                SOURCE_ID,
                EVENT_TYPE_ID,
                API_URL,
                PROP_NAME,
                WEBHOOK_PARAMS,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                eventSourceWebhook.sourceId,
                eventSourceWebhook.eventTypeId,
                eventSourceWebhook.apiUrl,
                eventSourceWebhook.propName,
                eventSourceWebhook.webhookParams,
                now,
                now
            )
                .execute()
        }
    }

    fun update(dslContext: DSLContext, eventSourceWebhook: EventSourceWebhook) {
        val now = LocalDateTime.now()
        with(TEventSourceWebhook.T_EVENT_SOURCE_WEBHOOK) {
            dslContext.update(this)
                .set(SOURCE_ID, eventSourceWebhook.sourceId)
                .set(EVENT_TYPE_ID, eventSourceWebhook.eventTypeId)
                .set(API_URL, eventSourceWebhook.apiUrl)
                .set(PROP_NAME, eventSourceWebhook.propName)
                .set(WEBHOOK_PARAMS, eventSourceWebhook.webhookParams)
                .set(UPDATE_TIME, now)
                .where(ID.eq(eventSourceWebhook.id))
                .execute()
        }
    }


    fun list(
        dslContext: DSLContext,
        sourceId: Long?,
        eventTypeId: Long?,
        offset: Int,
        limit: Int
    ) : List<EventSourceWebhook> {
        with(TEventSourceWebhook.T_EVENT_SOURCE_WEBHOOK) {
            val conditions = mutableListOf<Condition>()
            if (sourceId != null) {
                conditions.add(SOURCE_ID.eq(sourceId))
            }
            if (eventTypeId != null) {
                conditions.add(EVENT_TYPE_ID.eq(eventTypeId))
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
        eventTypeId: Long?
    ): Long {
        with(TEventSourceWebhook.T_EVENT_SOURCE_WEBHOOK) {
            val conditions = mutableListOf<Condition>()
            if (sourceId != null) {
                conditions.add(SOURCE_ID.eq(sourceId))
            }
            if (eventTypeId != null) {
                conditions.add(EVENT_TYPE_ID.eq(eventTypeId))
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
        sourceId: Long,
        eventTypeId: Long
    ): EventSourceWebhook? {
        val record = with(TEventSourceWebhook.T_EVENT_SOURCE_WEBHOOK) {
            dslContext.selectFrom(this)
                .where(SOURCE_ID.eq(sourceId))
                .and(EVENT_TYPE_ID.eq(eventTypeId))
                .fetchOne()
        } ?: return null
        return convert(record)
    }

    fun convert(record: TEventSourceWebhookRecord): EventSourceWebhook {
        return with(record) {
            EventSourceWebhook(
                id = id,
                sourceId = sourceId,
                eventTypeId = eventTypeId,
                apiUrl = apiUrl,
                propName = propName,
                webhookParams = webhookParams,
                createTime = createTime.timestampmilli(),
                updateTime = updateTime.timestampmilli()
            )
        }
    }
}
