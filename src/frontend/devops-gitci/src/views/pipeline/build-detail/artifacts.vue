<template>
    <article class="detail-artifactory-home" v-bkloading="{ isLoading }">
        <bk-table :data="artifactories"
            :outer-border="false"
            :header-border="false"
            :header-cell-style="{ background: '#f1f2f3' }"
            empty-text="No artifacts yet"
        >
            <bk-table-column label="Name" width="220" prop="name" show-overflow-tooltip></bk-table-column>
            <bk-table-column label="Path" prop="fullName" show-overflow-tooltip></bk-table-column>
            <bk-table-column label="Size" width="150" prop="size" :formatter="sizeFormatter" show-overflow-tooltip></bk-table-column>
            <bk-table-column label="Type" width="150" prop="artifactoryType" :formatter="repoTypeFormatter" show-overflow-tooltip></bk-table-column>
            <bk-table-column label="Operation" width="150">
                <template slot-scope="props">
                    <bk-button text
                        @click="downLoadFile(props.row)"
                        :disabled="!hasPermission"
                        v-bk-tooltips="{ content: 'You do not have the permission to download components of the pipeline, and you cannot download', disabled: hasPermission }"
                    >Download</bk-button>
                </template>
            </bk-table-column>
        </bk-table>
    </article>
</template>

<script>
    import { convertFileSize } from '@/utils'
    import { pipelines } from '@/http'
    import { mapState } from 'vuex'

    export default {
        data () {
            return {
                hasPermission: true,
                isLoading: true,
                artifactories: []
            }
        },

        computed: {
            ...mapState(['projectId', 'curPipeline'])
        },

        created () {
            this.initData()
        },

        methods: {
            initData () {
                const postData = {
                    projectId: this.projectId,
                    params: {
                        props: {
                            buildId: this.$route.params.buildId,
                            pipelineId: this.curPipeline.pipelineId
                        }
                    }
                }
                const permissionData = {
                    projectId: this.projectId,
                    pipelineId: this.curPipeline.pipelineId,
                    permission: 'DOWNLOAD'
                }
                this.isLoading = true
                Promise.all([
                    pipelines.requestPartFile(postData),
                    pipelines.requestExecPipPermission(permissionData)
                ]).then(([res, permission]) => {
                    this.artifactories = res.records || []
                    this.hasPermission = permission
                    if (this.artifactories.length <= 0) {
                        this.$emit('hidden')
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                    this.$emit('complete')
                })
            },
            downLoadFile (row) {
                Promise.all([
                    pipelines.requestDevnetGateway(),
                    pipelines.requestDownloadUrl({
                        projectId: this.projectId,
                        artifactoryType: row.artifactoryType,
                        path: row.path
                    })
                ]).then(([isDevnet, res]) => {
                    const url = isDevnet ? res.url : res.url2
                    window.location.href = url
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },
            repoTypeFormatter (row, column, cellValue, index) {
                const typeMap = {
                    CUSTOM_DIR: 'Custom Artifactory',
                    PIPELINE: 'Pipeline Artifactory'
                }
                return typeMap[cellValue]
            },
            sizeFormatter (row, column, cellValue, index) {
                return (cellValue >= 0 && convertFileSize(cellValue, 'B')) || ''
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .detail-artifactory-home {
        padding: 32px;
        /deep/ .bk-table {
            border: none;
            height: 100%;
            &::before {
                background-color: #fff;
            }
            td, th.is-leaf {
                border-bottom-color: #f0f1f5;
            }
            .bk-table-body-wrapper {
                max-height: calc(100% - 43px);
                overflow-y: auto;
            }
            .cell {
                overflow: hidden;
            }
            .bk-table-header, .bk-table-body {
                width: auto !important;
            }
        }
    }
</style>
