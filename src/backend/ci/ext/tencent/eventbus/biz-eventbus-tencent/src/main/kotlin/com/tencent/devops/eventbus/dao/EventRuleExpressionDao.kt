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
import com.tencent.devops.eventbus.pojo.EventRuleExpression
import com.tencent.devops.model.eventbus.tables.TEventRuleExpression
import com.tencent.devops.model.eventbus.tables.records.TEventRuleExpressionRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class EventRuleExpressionDao {

    fun create(dslContext: DSLContext, eventRuleExpression: EventRuleExpression) {
        val now = LocalDateTime.now()
        with(TEventRuleExpression.T_EVENT_RULE_EXPRESSION) {
            dslContext.insertInto(
                this,
                SOURCE_ID,
                EVENT_TYPE_ID,
                FILTER_NAME,
                EXPRESSION,
                DESC,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                eventRuleExpression.sourceId,
                eventRuleExpression.eventTypeId,
                eventRuleExpression.filterName,
                eventRuleExpression.expression,
                eventRuleExpression.desc,
                now,
                now
            )
                .execute()
        }
    }

    fun update(dslContext: DSLContext, eventRuleExpression: EventRuleExpression) {
        val now = LocalDateTime.now()
        with(TEventRuleExpression.T_EVENT_RULE_EXPRESSION) {
            dslContext.update(this)
                .set(SOURCE_ID, eventRuleExpression.sourceId)
                .set(EVENT_TYPE_ID, eventRuleExpression.eventTypeId)
                .set(FILTER_NAME, eventRuleExpression.filterName)
                .set(EXPRESSION, eventRuleExpression.expression)
                .set(DESC, eventRuleExpression.desc)
                .set(UPDATE_TIME, now)
                .execute()
        }
    }

    fun list(
        dslContext: DSLContext,
        sourceId: Long?,
        eventTypeId: Long?,
        filterName: String?,
        offset: Int,
        limit: Int
    ) : List<EventRuleExpression> {
        with(TEventRuleExpression.T_EVENT_RULE_EXPRESSION) {
            val conditions = mutableListOf<Condition>()
            if (sourceId != null) {
                conditions.add(SOURCE_ID.eq(sourceId))
            }
            if (eventTypeId != null) {
                conditions.add(EVENT_TYPE_ID.eq(eventTypeId))
            }
            if (filterName != null) {
                conditions.add(FILTER_NAME.eq(filterName))
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
        eventTypeId: Long?,
        filterName: String?
    ): Long {
        with(TEventRuleExpression.T_EVENT_RULE_EXPRESSION) {
            val conditions = mutableListOf<Condition>()
            if (sourceId != null) {
                conditions.add(SOURCE_ID.eq(sourceId))
            }
            if (eventTypeId != null) {
                conditions.add(EVENT_TYPE_ID.eq(eventTypeId))
            }
            if (filterName != null) {
                conditions.add(FILTER_NAME.eq(filterName))
            }
            val where = if (conditions.isNotEmpty()) {
                dslContext.selectCount().from(this).where(conditions)
            } else {
                dslContext.selectCount().from(this)
            }
            return where.fetchOne(0, Long::class.java)!!
        }
    }

    fun getByFilterNames(
        dslContext: DSLContext,
        sourceId: Long,
        eventTypeId: Long,
        filterNames: List<String>
    ): List<EventRuleExpression> {
        return with(TEventRuleExpression.T_EVENT_RULE_EXPRESSION) {
            dslContext.selectFrom(this)
                .where(SOURCE_ID.eq(sourceId))
                .and(EVENT_TYPE_ID.eq(eventTypeId))
                .and(FILTER_NAME.`in`(filterNames))
                .fetch()
                .map { convert(it) }
        }
    }

    fun convert(record: TEventRuleExpressionRecord): EventRuleExpression {
        return with(record) {
            EventRuleExpression(
                id = id,
                sourceId = sourceId,
                eventTypeId = eventTypeId,
                filterName = filterName,
                expression = expression,
                desc = desc,
                createTime = createTime.timestampmilli(),
                updateTime = updateTime.timestampmilli()
            )
        }
    }
}
