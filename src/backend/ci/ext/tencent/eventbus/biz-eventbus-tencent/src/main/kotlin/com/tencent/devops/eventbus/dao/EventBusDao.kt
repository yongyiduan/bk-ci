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
import com.tencent.devops.eventbus.pojo.EventBus
import com.tencent.devops.model.eventbus.tables.TEventBus
import com.tencent.devops.model.eventbus.tables.records.TEventBusRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class EventBusDao {

    fun create(dslContext: DSLContext, eventBus: EventBus) {
        val now = LocalDateTime.now()
        with(TEventBus.T_EVENT_BUS) {
            dslContext.insertInto(
                this,
                BUS_ID,
                PROJECT_ID,
                NAME,
                DESC,
                CREATE_TIME,
                CREATOR,
                UPDATE_TIME,
                UPDATER
            )
                .values(
                    eventBus.busId,
                    eventBus.projectId,
                    eventBus.name,
                    eventBus.desc,
                    now,
                    eventBus.creator,
                    now,
                    eventBus.updater
                )
                .execute()
        }
    }

    fun getByBusId(
        dslContext: DSLContext,
        projectId: String,
        busId: String
    ): EventBus? {
        val record = with(TEventBus.T_EVENT_BUS) {
            dslContext.selectFrom(this)
                .where(PROJECT_ID.eq(projectId))
                .and(BUS_ID.eq(busId))
                .fetchOne()
        } ?: return null
        return convert(record)
    }

    fun convert(record: TEventBusRecord): EventBus {
        return with(record) {
            EventBus(
                busId = busId,
                projectId = projectId,
                name = name,
                desc = desc,
                createTime = createTime.timestampmilli(),
                creator = creator,
                updateTime = updateTime.timestampmilli(),
                updater = updater
            )
        }
    }
}
