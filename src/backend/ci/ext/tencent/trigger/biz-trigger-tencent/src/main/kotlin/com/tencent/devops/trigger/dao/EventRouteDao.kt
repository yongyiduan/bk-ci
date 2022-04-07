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
import com.tencent.devops.model.trigger.tables.TEventRoute
import com.tencent.devops.model.trigger.tables.records.TEventRouteRecord
import com.tencent.devops.trigger.pojo.EventRoute
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class EventRouteDao {

    fun create(dslContext: DSLContext, eventRoute: EventRoute) {
        val now = LocalDateTime.now()
        with(TEventRoute.T_EVENT_ROUTE) {
            dslContext.insertInto(
                this,
                SOURCE,
                THIRD_ID,
                PROJECT_ID,
                BUS_ID,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                eventRoute.source,
                eventRoute.thirdId,
                eventRoute.projectId,
                eventRoute.busId,
                now,
                now
            ).onDuplicateKeyIgnore()
                .execute()
        }
    }

    fun batchCreate(dslContext: DSLContext, eventRoutes: List<EventRoute>) {
        eventRoutes.forEach { eventRoute ->
            create(dslContext = dslContext, eventRoute = eventRoute)
        }
    }

    fun listByThirdId(
        dslContext: DSLContext,
        source: String,
        thirdId: String
    ) : List<EventRoute> {
        return with(TEventRoute.T_EVENT_ROUTE) {
            dslContext.selectFrom(this)
                .where(SOURCE.eq(source))
                .and(THIRD_ID.eq(thirdId))
                .fetch()
        }.map { convert(it) }
    }

    fun convert(record: TEventRouteRecord): EventRoute {
        return with(record) {
            EventRoute(
                id = id,
                source = source,
                thirdId = thirdId,
                projectId = projectId,
                busId = busId,
                createTime = createTime.timestampmilli(),
                updateTime = updateTime.timestampmilli()
            )
        }
    }

    fun deleteByBusId(dslContext: DSLContext, busId: String, projectId: String) {
        with(TEventRoute.T_EVENT_ROUTE) {
            dslContext.deleteFrom(this)
                .where(BUS_ID.eq(busId))
                .and(PROJECT_ID.eq(projectId))
                .execute()
        }
    }
}
