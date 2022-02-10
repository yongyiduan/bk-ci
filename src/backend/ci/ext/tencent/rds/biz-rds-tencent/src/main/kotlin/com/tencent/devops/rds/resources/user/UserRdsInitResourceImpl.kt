package com.tencent.devops.rds.resources.user

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.rds.api.user.UserRdsInitResource
import com.tencent.devops.rds.pojo.RdsInitInfo

@RestResource
class UserRdsInitResourceImpl : UserRdsInitResource {
    override fun init(userId: String, info: RdsInitInfo): Result<Boolean> {
        return Result(true)
    }
}
