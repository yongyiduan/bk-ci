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
import com.tencent.devops.process.yaml.modelCreate.TXModelCreate
import com.tencent.devops.process.yaml.modelCreate.inner.ModelCreateEvent
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.regex.Pattern
import org.slf4j.LoggerFactory

@Service
class StreamConverter @Autowired constructor(
    private val modelCreate: TXModelCreate
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StreamConverter::class.java)
    }

    fun buildModel(
        userId: String,
        productId: Long,
        projectId: String,
        cachePath: String,
        pipelineFile: File
    ): StreamBuildResult {
        val pipelineYaml = FileUtils.readFileToString(pipelineFile, StandardCharsets.UTF_8).ifBlank {
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.formatErrorMessage,
                params = arrayOf(pipelineFile.name)
            )
        }

        val (preYamlObject, yamlObject) = replaceTemplate(
            cachePath = cachePath,
            fileName = pipelineFile.name,
            pipelineYaml = pipelineYaml
        )

        logger.debug("RDS|buildModel|parse yml[${pipelineFile.name}]|" +
            "preYamlObject=\n$preYamlObject\nyamlObject=\n$yamlObject")

        val model = modelCreate.createPipelineModel(
            modelName = "",
            event = ModelCreateEvent(
                userId,
                projectCode = projectId
            ),
            yaml = yamlObject,
            pipelineParams = ChartPipelineStartParams.emptyPipelineParams()
        )

        return StreamBuildResult(
            originYaml = pipelineYaml,
            parsedYaml = YamlCommonUtils.toYamlNotNull(preYamlObject),
            pipelineModel = model.model
        )
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
                    defaultMessage = ChartErrorCodeEnum.READ_CHART_FILE_BLANK_ERROR.formatErrorMessage,
                    params = arrayOf(path.name)
                )
            }
            // 针对可能会与go template冲突的字段进行替换
            val newContent = replaceTemplatePlaceholder(content)
            return ScriptYmlUtils.formatYaml(newContent)
        }
    }

    /**
     * // TODO: 临时方案
     * go template 使用 {{}} 会与现在的 stream的 ${{}} 产生冲突
     * 目前方案是在 origin中使用 ${| }} 在这个函数中再换回 ${{}}
     */
    private fun replaceTemplatePlaceholder(content: String): String {
        var newContent = content
        val pattern = Pattern.compile("\\$\\{\\|([^{}]+?)}}")
        val matcher = pattern.matcher(content)
        while (matcher.find()) {
            val value = matcher.group()
            newContent = newContent.replace(matcher.group(), "\${{${value.removePrefix("\${|")}")
        }
        return newContent
    }

    private fun formatYaml(
        yaml: String
    ): PreTemplateScriptBuildYaml {
        return Yaml.unMarshal(ScriptYmlUtils.formatYaml(yaml))
    }
}
