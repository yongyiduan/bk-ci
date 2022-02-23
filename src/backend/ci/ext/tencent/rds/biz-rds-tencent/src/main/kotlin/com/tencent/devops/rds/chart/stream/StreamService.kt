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

package com.tencent.devops.rds.chart.stream

import com.devops.process.yaml.modelCreate.ModelCreate
import com.devops.process.yaml.modelCreate.inner.ModelCreateEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.tencent.devops.common.ci.v2.PreScriptBuildYaml
import com.tencent.devops.common.ci.v2.PreTemplateScriptBuildYaml
import com.tencent.devops.common.ci.v2.ScriptBuildYaml
import com.tencent.devops.common.ci.v2.parsers.template.YamlTemplate
import com.tencent.devops.common.ci.v2.parsers.template.models.GetTemplateParam
import com.tencent.devops.common.ci.v2.utils.ScriptYmlUtils
import com.tencent.devops.common.ci.v2.utils.YamlCommonUtils
import com.tencent.devops.common.client.Client
import com.tencent.devops.rds.utils.Yaml
import java.io.File
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class StreamService @Autowired constructor(
    private val client: Client,
    private val objectMapper: ObjectMapper,
    private val modelCreateInner: ModelCreateInnerImpl
) {

    private val modelCreate = ModelCreate(client, objectMapper, modelCreateInner)

    fun buildModel(userId: String, projectId: String, cachePath: String, pipelineFile: File): StreamBuildResult {
        val pipelineYaml = FileUtils.readFileToString(pipelineFile, StandardCharsets.UTF_8)

        val (preYamlObject, yamlObject) = replaceTemplate(cachePath, pipelineFile.name, pipelineYaml)

        val model = modelCreate.createPipelineModel(
            modelName = "${pipelineFile.name}_${System.currentTimeMillis()}",
            event = ModelCreateEvent(
                userId,
                projectCode = projectId
            ),
            yaml = yamlObject,
            pipelineParams = emptyList()
        )

        return StreamBuildResult(
            originYaml = pipelineYaml,
            parsedYaml = YamlCommonUtils.toYamlNotNull(preYamlObject),
            pipelineModel = model
        )
    }

    private fun replaceTemplate(
        cachePath: String,
        fileName: String,
        pipelineYaml: String
    ): Pair<PreScriptBuildYaml, ScriptBuildYaml> {
        val preYamlObject = YamlTemplate(
            yamlObject = formatYaml(pipelineYaml),
            filePath = fileName,
            extraParameters = TemplateExtraParams(
                cachePath = cachePath
            ),
            getTemplateMethod = this::getTemplate,
            nowRepo = null,
            repo = null
        ).replace()

        return Pair(preYamlObject, ScriptYmlUtils.normalizeRdsYaml(preYamlObject, fileName))
    }

    private fun getTemplate(
        param: GetTemplateParam<TemplateExtraParams>
    ): String {
        with(param) {
            return FileUtils.readFileToString(File("${extraParameters.cachePath}/$path"), StandardCharsets.UTF_8)
        }
    }

    private fun formatYaml(
        yaml: String
    ): PreTemplateScriptBuildYaml {
        return Yaml.unMarshal(ScriptYmlUtils.formatYaml(yaml))
    }
}
