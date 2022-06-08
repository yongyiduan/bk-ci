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

package com.tencent.devops.rds.chart

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.pipeline.Model
import com.tencent.devops.process.yaml.modelCreate.TXModelCreate
import com.tencent.devops.process.yaml.modelCreate.inner.ModelCreateEvent
import com.tencent.devops.process.yaml.modelCreate.inner.PipelineInfo
import com.tencent.devops.process.yaml.modelCreate.utils.TXScriptYmlUtils
import com.tencent.devops.process.yaml.v2.models.PreScriptBuildYaml
import com.tencent.devops.process.yaml.v2.models.PreTemplateScriptBuildYaml
import com.tencent.devops.process.yaml.v2.models.ScriptBuildYaml
import com.tencent.devops.process.yaml.v2.parsers.template.YamlTemplate
import com.tencent.devops.process.yaml.v2.parsers.template.models.GetTemplateParam
import com.tencent.devops.process.yaml.v2.utils.ScriptYmlUtils
import com.tencent.devops.process.yaml.v2.utils.YamlCommonUtils
import com.tencent.devops.rds.chart.stream.StreamBuildResult
import com.tencent.devops.rds.chart.stream.TemplateExtraParams
import com.tencent.devops.rds.constants.Constants
import com.tencent.devops.rds.exception.ChartErrorCodeEnum
import com.tencent.devops.rds.utils.Yaml
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

@Service
class StreamConverter @Autowired constructor(
    private val modelCreate: TXModelCreate
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StreamConverter::class.java)
    }

    fun parseTemplate(
        userId: String,
        productCode: String,
        projectId: String,
        cachePath: String,
        pipelineFile: File,
        init: Boolean? = false
    ): StreamBuildResult {
        val pipelineYaml = FileUtils.readFileToString(pipelineFile, StandardCharsets.UTF_8).ifBlank {
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.formatErrorMessage
                    .format(pipelineFile.name)
            )
        }

        val (preYamlObject, yamlObject) = replaceTemplate(
            cachePath = cachePath,
            fileName = pipelineFile.name,
            pipelineYaml = pipelineYaml
        )

        logger.debug(
            "RDS|buildModel|parse yml[${pipelineFile.name}]|" +
                "preYamlObject=\n$preYamlObject\nyamlObject=\n$yamlObject"
        )

        return StreamBuildResult(
            originYaml = pipelineYaml,
            parsedYaml = YamlCommonUtils.toYamlNotNull(preYamlObject),
            yamlObject = yamlObject
        )
    }

    fun getYamlModel(
        userId: String,
        productCode: String,
        projectId: String,
        pipelineId: String,
        pipelineName: String,
        yamlObject: ScriptBuildYaml,
        init: Boolean? = false
    ): Model {

        val model = modelCreate.createPipelineModel(
            modelName = pipelineName,
            event = ModelCreateEvent(
                userId,
                projectCode = projectId,
                pipelineInfo = PipelineInfo(pipelineId)
            ),
            yaml = yamlObject,
            pipelineParams = ChartPipelineStartParams.makePipelineParams(init == true, yamlObject.variables)
        )

        return model.model
    }

    fun replaceTemplate(
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

        return Pair(preYamlObject, TXScriptYmlUtils.normalizeRdsYaml(preYamlObject, fileName))
    }

    private fun getTemplate(
        param: GetTemplateParam<TemplateExtraParams>
    ): String {
        with(param) {
            val path = File(Paths.get(extraParameters.cachePath, Constants.CHART_TEMPLATE_DIR, path).toUri())
            val content = FileUtils.readFileToString(path, StandardCharsets.UTF_8).ifBlank {
                throw ErrorCodeException(
                    errorCode = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.errorCode,
                    defaultMessage = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.formatErrorMessage.format(path.name)
                )
            }

            return ScriptYmlUtils.formatYaml(content)
        }
    }

    private fun formatYaml(
        yaml: String
    ): PreTemplateScriptBuildYaml {
        return Yaml.unMarshal(ScriptYmlUtils.formatYaml(yaml))
    }
}
