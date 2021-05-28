<template>
    <article class="detail-report-home" v-bkloading="{ isLoading }">
        <template v-if="reportList.length">
            <bk-tab :active.sync="reportIndex">
                <bk-tab-panel
                    v-for="(report, index) in reportList"
                    v-bind="report"
                    :key="index">
                </bk-tab-panel>
            </bk-tab>

            <bk-table :data="chooseReport.thirdReports"
                :outer-border="false"
                :header-border="false"
                :header-cell-style="{ background: '#FAFBFD' }"
                v-if="chooseReport.type === 'THIRDPARTY'"
                class="report-file"
            >
                <bk-table-column label="Name" show-overflow-tooltip>
                    <template slot-scope="props">
                        <icon name="tiaozhuan" size="18" class="jump-icon" />
                        <a :href="props.row.indexFileUrl" target="_blank" class="text-link">{{ props.row.name }}</a>
                    </template>
                </bk-table-column>
            </bk-table>
            <iframe :src="chooseReport.indexFileUrl" frameborder="0" class="report-file" v-else></iframe>
        </template>
        <span class="bk-table-empty-text" v-if="!isLoading && reportList.length <= 0">
            <i class="bk-table-empty-icon bk-icon icon-empty"></i>
            <div>No reports yet</div>
        </span>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { pipelines } from '@/http'

    export default {
        data () {
            return {
                isLoading: false,
                reportList: [],
                reportIndex: 0
            }
        },

        computed: {
            ...mapState(['projectId', 'curPipeline']),

            chooseReport () {
                return this.reportList.find((report, index) => (index === this.reportIndex)) || {}
            }
        },

        created () {
            this.initData()
        },

        methods: {
            initData () {
                const postData = {
                    projectId: this.projectId,
                    pipelineId: this.curPipeline.pipelineId,
                    buildId: this.$route.params.buildId
                }
                this.isLoading = true
                pipelines.requestReportList(postData).then((res) => {
                    const thirdReports = []
                    const innerReports = [];
                    (res || []).forEach((item) => {
                        if (item.type === 'THIRDPARTY') {
                            thirdReports.push(item)
                        } else {
                            innerReports.push(item)
                        }
                    })
                    this.reportList = innerReports
                    if (thirdReports.length) this.reportList.push({ name: 'Third party report', thirdReports, type: 'THIRDPARTY' })
                    this.reportList = this.reportList.map((report, index) => ({ name: index, label: report.name, indexFileUrl: report.indexFileUrl }))
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                    this.$emit('complete')
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .detail-report-home {
        /deep/ .bk-tab-section {
            padding: 0;
            border: none;
        }
    }
    .report-file {
        height: calc(100% - 50px);
        width: 100%;
        .jump-icon {
            fill: #3c96ff;
            vertical-align: bottom;
            margin-right: 3px;
        }
    }
    .bk-table-empty-text {
        width: 100px;
        margin: 0 auto;
        text-align: center;
        display: block;
    }
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
            overflow-x: hidden;
        }
        .cell {
            overflow: hidden;
        }
        .bk-table-header, .bk-table-body {
            width: auto !important;
        }
    }
</style>
