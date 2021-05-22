<template>
    <article class="build-detail-home" v-bkloading="{ isLoading }">
        <section class="detail-header">
            <build-status :status="buildDetail.status" class="header-icon"></build-status>
            <p class="detail-info">
                <span class="info-title">
                    {{ buildDetail.commitMsg }}
                    <span class="title-item"><icon :name="buildTypeIcon" size="14"></icon></span>
                    <span class="title-item"><img :src="`http://dayu.oa.com/avatars/${buildDetail.userId}/profile.jpg`">{{ buildDetail.userId }}</span>
                </span>
                <span class="info-data">
                    <span class="info-item text-ellipsis"><icon name="source-branch" size="14"></icon>{{ buildDetail.branch }}</span>
                    <span class="info-item text-ellipsis"><icon name="clock" size="14"></icon>{{ buildDetail.spendTime | spendTimeFilter }}</span>
                    <span class="info-item text-ellipsis">
                        <icon name="message" size="14"></icon>
                        {{ buildDetail.buildHistoryRemark || '--' }}
                        <bk-popconfirm trigger="click" @confirm="confirmUpdateRemark">
                            <div slot="content">
                                <h3 class="mb10">修改备注</h3>
                                <bk-input type="textarea" v-model="remark" placeholder="请输入备注" class="mb10"></bk-input>
                            </div>
                            <bk-icon type="edit2" style="font-size: 18px;cursor:pointer" />
                        </bk-popconfirm>
                    </span>
                </span>
                <span class="info-data">
                    <span class="info-item text-ellipsis"><icon name="commit" size="14"></icon>{{ buildDetail.commitId | commitFilter }}</span>
                    <span class="info-item text-ellipsis"><icon name="date" size="14"></icon>{{ buildDetail.startTime | timeFilter }}</span>
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
    import { preciseDiff, timeFormatter, commitIdFormatter, getbuildTypeIcon } from '@/utils'
    import stages from '@/components/stages'
    import buildStatus from '@/components/build-status'

    export default {
        components: {
            stages,
            buildStatus
        },

        filters: {
            spendTimeFilter (val) {
                return preciseDiff(val)
            },

            timeFilter (val) {
                return timeFormatter(val)
            },

            commitFilter (val) {
                return commitIdFormatter(val)
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
            ...mapState(['projectId']),

            buildTypeIcon () {
                return getbuildTypeIcon(this.buildDetail.objectKind)
            }
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
                        startTime: modelDetail.startTime,
                        status: modelDetail.status
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
                    this.getPipelineBuildDetail()
                    this.loopGetPipelineDetail()
                }, 2000)
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .build-detail-home {
        background: #fff;
    }
    .detail-header {
        padding: 10px 24px;
        height: 96px;
        box-shadow: 0 1px 0 0 #E3E5EA;
        display: flex;
        align-items: flex-start;
        .header-icon {
            font-size: 32px;
        }
        .detail-info {
            flex: 1;
            margin: 0 7px;
            .info-title {
                color: #313328;
                line-height: 24px;
                height: 24px;
                display: flex;
                align-items: center;
                margin: 4px 0 4px;
                .title-item {
                    position: relative;
                    display: flex;
                    align-items: center;
                    padding-left: 15px;
                    margin-left: 35px;
                    height: 20px;
                    color: #81838a;
                    font-size: 12px;
                    &:last-child {
                        margin-left: 15px;
                    }
                    img {
                        width: 20px;
                        height: 20px;
                        border-radius: 100%;
                        margin-right: 8px;
                    }
                    &:before {
                        position: absolute;
                        content: '';
                        left: 0;
                        height: 12px;
                        width: 1px;
                        background: #c8ccd8;
                    }
                }
            }
            .info-data {
                color: #81838a;
                line-height: 20px;
                font-size: 12px;
                display: flex;
                align-items: center;
            }
            .info-item {
                width: 200px;
                display: flex;
                align-items: center;
                margin-bottom: 2px;
                >svg {
                    margin-right: 8px;
                }
                &:not(:first-child) {
                    margin-left: 40px;
                }
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
