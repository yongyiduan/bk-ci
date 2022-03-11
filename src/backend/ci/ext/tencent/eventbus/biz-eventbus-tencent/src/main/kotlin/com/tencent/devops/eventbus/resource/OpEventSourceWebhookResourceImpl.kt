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

package com.tencent.devops.eventbus.resource

import com.tencent.devops.common.api.pojo.Page
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.PageUtil
import com.tencent.devops.eventbus.api.OpEventSourceWebhookResource
import com.tencent.devops.eventbus.dao.EventSourceWebhookDao
import com.tencent.devops.eventbus.pojo.EventSourceWebhook
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired

class OpEventSourceWebhookResourceImpl @Autowired constructor(
    private val dslContext: DSLContext,
    private val eventSourceWebhookDao: EventSourceWebhookDao
) : OpEventSourceWebhookResource {

    override fun create(eventSourceWebhook: EventSourceWebhook): Result<Boolean> {
        eventSourceWebhookDao.create(
            dslContext = dslContext,
            eventSourceWebhook = eventSourceWebhook
        )
        return Result(true)
    }

    override fun update(eventSourceWebhook: EventSourceWebhook): Result<Boolean> {
        eventSourceWebhookDao.update(
            dslContext = dslContext,
            eventSourceWebhook = eventSourceWebhook
        )
        return Result(true)
    }

    override fun list(
        sourceId: Long?,
        eventTypeId: Long?,
        page: Int?,
        pageSize: Int?
    ): Result<Page<EventSourceWebhook>> {
        val pageNotNull = page ?: 0
        val pageSizeNotNull = pageSize?.coerceAtMost(PageUtil.DEFAULT_PAGE_SIZE) ?: PageUtil.DEFAULT_PAGE_SIZE
        val limit = PageUtil.convertPageSizeToSQLLimit(pageNotNull, pageSizeNotNull)
        val count = eventSourceWebhookDao.count(
            dslContext = dslContext,
            sourceId = sourceId,
            eventTypeId = eventTypeId
        )
        val records = eventSourceWebhookDao.list(
            dslContext = dslContext,
            sourceId = sourceId,
            eventTypeId = eventTypeId,
            limit = limit.limit,
            offset = limit.offset
        )
        return Result(Page(pageNotNull, pageSizeNotNull, count, records))
    }
}
