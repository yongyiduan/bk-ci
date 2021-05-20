<template>
    <article class="build-detail-home" v-bkloading="{ isLoading }">
        <section class="detail-header">
            <i class="bk-icon icon-check-1"></i>
            <p class="detail-info">
                <span class="info-title">feat: dockerhost-buildImage支持多个tag，支持--target</span>
                <span class="info-data">
                    <span class="info-item">{{ buildDetail.branch }}</span>
                    <span class="info-item">{{ buildDetail.spendTime | spendTimeFilter }}</span>
                    <span class="info-item">{{ buildDetail.commitId }}</span>
                    <span class="info-item">{{ buildDetail.startTime }}</span>
                    <span class="info-item">{{ buildDetail.buildHistoryRemark || '--' }}</span>
                    <bk-popconfirm trigger="click" @confirm="confirmUpdateRemark">
                        <div slot="content">
                            <h3 class="mb10">修改备注</h3>
                            <bk-input type="textarea" v-model="remark" placeholder="请输入备注" class="mb10"></bk-input>
                        </div>
                        <bk-icon type="edit2" style="font-size: 18px;cursor:pointer" />
                    </bk-popconfirm>
                </span>
            </p>
            <bk-button class="detail-button" @click="rebuild" :loading="isRebuilding">重新构建</bk-button>
        </section>
        <stages :stages="stageList" class="detail-stages"></stages>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { pipelines } from '@/http'
    import stages from '@/components/stages'
    import { convertMStoString } from '@/utils'

    export default {
        components: {
            stages
        },

        filters: {
            spendTimeFilter (val) {
                return convertMStoString(val)
            }
        },

        data () {
            return {
                stageList: [],
                buildDetail: {},
                isLoading: false,
                isRebuilding: false,
                remark: ''
            }
        },

        computed: {
            ...mapState(['projectId'])
        },

        created () {
            this.initData()
        },

        beforeDestroy () {
            clearTimeout(this.loopGetPipelineDetail.loopId)
        },

        methods: {
            initData () {
                this.isLoading = true
                this.getPipelineBuildDetail().then(() => {
                    this.loopGetPipelineDetail()
                }).finally(() => {
                    this.isLoading = false
                })
            },

            getPipelineBuildDetail () {
                const params = {
                    pipelineId: this.$route.params.pipelineId,
                    buildId: this.$route.params.buildId
                }
                return pipelines.getPipelineBuildDetail(this.projectId, params).then((res) => {
                    const modelDetail = res.modelDetail || {}
                    const model = modelDetail.model || {}
                    this.stageList = model.stages
                    this.remark = res.buildHistoryRemark
                    this.buildDetail = {
                        ...res.gitProjectPipeline,
                        ...res.gitRequestEvent,
                        buildHistoryRemark: res.buildHistoryRemark,
                        spendTime: modelDetail.endTime - modelDetail.startTime,
                        startTime: modelDetail.startTime
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            rebuild () {
                this.isRebuilding = true
                pipelines.rebuildPipeline(this.projectId, this.$route.params.pipelineId, this.$route.params.buildId).then(() => {
                    this.loopGetPipelineDetail()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isRebuilding = false
                })
            },

            confirmUpdateRemark () {
                pipelines.updateRemark(this.projectId, this.$route.params.pipelineId, this.$route.params.buildId, this.remark).then(() => {
                    this.getPipelineBuildDetail()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            loopGetPipelineDetail () {
                clearTimeout(this.loopGetPipelineDetail.loopId)
                this.loopGetPipelineDetail.loopId = setTimeout(() => {
                    this.getPipelineBuildDetail().then(() => {
                        this.loopGetPipelineDetail()
                    })
                }, 5000)
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .build-detail-home {
        background: #fff;
    }
    .detail-header {
        padding: 16px 24px;
        height: 96px;
        box-shadow: 0 1px 0 0 #E3E5EA;
        display: flex;
        align-items: flex-start;
        .icon-check-1 {
            font-size: 32px;
        }
        .detail-info {
            flex: 1;
            margin: 0 7px;
            .info-title {
                color: #313328;
                line-height: 24px;
                height: 24px;
                display: inline-block;
                margin: 4px 0 10px;
            }
            .info-data {
                color: #81838a;
                line-height: 20px;
                font-size: 12px;
                display: flex;
                align-items: center;
            }
            .info-item:not(:first-child) {
                margin-left: 40px;
            }
            .history-remark {
                margin-bottom: 15px;
            }
        }
        .detail-button {
            margin-top: 16px;
        }
    }
    .detail-stages {
        height: calc(100% - 96px);
        padding: 30px 0;
        margin: 0 30px;
        overflow: auto;
    }
</style>
