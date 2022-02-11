package com.tencent.devops.rds.resources.user

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.rds.api.user.UserRdsInitResource
import com.tencent.devops.rds.pojo.RdsInitInfo
import org.slf4j.LoggerFactory

@RestResource
class UserRdsInitResourceImpl : UserRdsInitResource {

    companion object {
        private val logger = LoggerFactory.getLogger(UserRdsInitResourceImpl::class.java)
    }

    override fun init(userId: String, info: RdsInitInfo): Result<Boolean> {

        logger.info("init test: $userId $info")

        return Result(true)
    }
}
