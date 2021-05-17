<template>
    <article class="build-detail-home" v-bkloading="{ isLoading }">
        <section class="detail-header">
            <i class="bk-icon icon-check-1"></i>
            <p class="detail-info">
                <span class="info-title">feat: dockerhost-buildImage支持多个tag，支持--target</span>
                <span class="info-data">
                    <span class="info-item">master</span>
                    <span class="info-item">38 seconds</span>
                    <span class="info-item">3534deffe</span>
                    <span class="info-item">2021-03-23 23:12:38</span>
                </span>
            </p>
            <bk-button class="detail-button">重新构建</bk-button>
        </section>
        <stages :stages="stageList" class="detail-stages"></stages>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { pipelines } from '@/http'
    import stages from '@/components/stages'

    export default {
        components: {
            stages
        },

        data () {
            return {
                stageList: [],
                buildDetail: {},
                isLoading: false
            }
        },

        computed: {
            ...mapState(['projectId', 'curPipeline'])
        },

        created () {
            this.getPipelineBuildDetail()
        },

        methods: {
            getPipelineBuildDetail () {
                const params = {
                    pipelineId: this.curPipeline.pipelineId,
                    buildId: this.$route.params.buildId
                }
                this.isLoading = true
                pipelines.getPipelineBuildDetail(this.projectId, params).then((res) => {
                    const modelDetail = res.modelDetail || {}
                    const model = modelDetail.model || {}
                    this.stageList = model.stages
                    this.buildDetail = {
                        ...res.gitProjectPipeline,
                        ...res.gitRequestEvent
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
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
