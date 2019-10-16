package com.tencent.devops.artifactory.resources.builds

import com.tencent.devops.artifactory.api.builds.BuildCustomDirResource
import com.tencent.devops.artifactory.pojo.CombinationPath
import com.tencent.devops.artifactory.pojo.FileInfo
import com.tencent.devops.artifactory.pojo.PathList
import com.tencent.devops.artifactory.pojo.PathPair
import com.tencent.devops.artifactory.service.BuildCustomDirService
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class BuildCustomDirResourceImpl @Autowired constructor(
    val buildCustomDirService: BuildCustomDirService
) : BuildCustomDirResource {

    override fun list(projectId: String, path: String): List<FileInfo> {
        if (path.contains(".")) {
            throw RuntimeException("please confirm the param is directory...")
        } else {
            return buildCustomDirService.list(projectId, path)
        }
    }

    override fun mkdir(projectId: String, path: String): Result<Boolean> {
        buildCustomDirService.mkdir(projectId, path)
        return Result(true)
    }

    override fun rename(projectId: String, pathPair: PathPair): Result<Boolean> {
        buildCustomDirService.rename(projectId, pathPair.srcPath, pathPair.destPath)
        return Result(true)
    }

    override fun copy(projectId: String, combinationPath: CombinationPath): Result<Boolean> {
        buildCustomDirService.copy(projectId, combinationPath)
        return Result(true)
    }

    override fun move(projectId: String, combinationPath: CombinationPath): Result<Boolean> {
        buildCustomDirService.move(projectId, combinationPath)
        return Result(true)
    }

    override fun delete(projectId: String, pathList: PathList): Result<Boolean> {
        buildCustomDirService.delete(projectId, pathList)
        return Result(true)
    }
}