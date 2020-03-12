<template>
    <article v-bkloading="{ isLoading }" class="service-progress-home">
        <h3 class="market-home-title">
            <icon class="title-icon" name="color-logo-store" size="25" />
            <p class="title-name">
                <span class="back-home" @click="toAtomStore"> {{ $t('store.研发商店') }} </span>
                <i class="right-arrow banner-arrow"></i>
                <span class="banner-des back-home" @click="toServiceList"> {{ $t('store.工作台') }} </span>
                <i class="right-arrow banner-arrow"></i>
                <span class="banner-des">{{$t('store.上架/升级扩展')}}（{{serviceDetail.serviceCode}}）</span>
            </p>
            <a class="title-work" target="_blank" href="http://tempdocklink/pages/viewpage.action?pageId=22118721"> {{ $t('store.扩展指引') }} </a>
        </h3>
        <article v-if="!isLoading" class="service-progress-main">
            <header class="progress-header">
                <bk-process :list="progressStatus" :cur-process="currentStepIndex" display-key="name" class="progress-steps"></bk-process>
                <bk-button class="progress-cancle"> {{ $t('store.中止发布') }} </bk-button>
            </header>

            <section class="progress-main">
                <component :is="currentStep.code" :current-step="currentStep" :detail="serviceDetail"></component>
            </section>
        </article>
    </article>
</template>

<script>
    import { mapActions } from 'vuex'
    import build from '../components/common/progressSteps/build'
    import test from '../components/common/progressSteps/test'
    import commit from '../components/common/progressSteps/commit'
    import approve from '../components/common/progressSteps/approve'
    import begin from '../components/common/progressSteps/begin'

    export default {
        components: {
            build,
            test,
            commit,
            approve,
            begin
        },

        data () {
            return {
                isLoading: false,
                isTestLoading: false,
                progressStatus: [],
                currentStepIndex: 1,
                isOver: false,
                serviceDetail: {},
                storeBuildInfo: {},
                permission: true,
                currentProjectId: '',
                currentBuildNo: '',
                currentPipelineId: ''
            }
        },

        computed: {
            permissionMsg () {
                let str = ''
                if (!this.permission) str = this.$t('store.只有扩展管理员或当前流程创建者可以操作')
                return str
            },

            currentStep () {
                return this.progressStatus[this.currentStepIndex - 1]
            }
        },

        created () {
            this.initData()
            // this.loopProgress()
        },

        beforeDestroy () {
            clearTimeout(this.loopProgress.timeId)
        },

        methods: {
            ...mapActions('store', [
                'requestServiceDetail',
                'requestServiceProcess',
                'requestServiceCancelRelease',
                'requestServicePassTest',
                'requestRecheckService'
            ]),

            initData () {
                Promise.all([this.getServiceDetail(), this.getServiceProcess()]).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            reCheck () {
                if (!this.permission) return

                const params = this.$route.params || {}
                const serviceId = params.serviceId || ''
                this.requestRecheckService(serviceId).then(() => {
                    this.$bkMessage({ message: this.$t('store.发起重新验证成功'), theme: 'success' })
                    this.getServiceProcess()
                }).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                })
            },

            passTest () {
                if (!this.permission) return

                const params = this.$route.params || {}
                const serviceId = params.serviceId || ''
                this.isTestLoading = true
                this.requestServicePassTest(serviceId).then(() => {
                    this.getServiceProcess()
                }).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => (this.isTestLoading = false))
            },

            cancelRelease () {
                if (!this.permission) return

                const confirmFn = () => {
                    const params = this.$route.params || {}
                    const serviceId = params.serviceId || ''
                    this.requestServiceCancelRelease(serviceId).then(() => {
                        this.$bkMessage({ message: this.$t('store.取消发布成功'), theme: 'success' })
                        this.toServiceList()
                    }).catch((err) => {
                        this.$bkMessage({ message: err.message || err, theme: 'error' })
                    }).finally(() => (this.isLoading = false))
                }
                this.$bkInfo({
                    title: this.$t('store.确认要取消发布吗？'),
                    confirmFn
                })
            },

            getServiceDetail () {
                const params = this.$route.params || {}
                const serviceId = params.serviceId || ''
                this.isLoading = true
                return this.requestServiceDetail(serviceId).then((res) => {
                    this.serviceDetail = res
                })
            },

            getServiceProcess () {
                const params = this.$route.params || {}
                const serviceId = params.serviceId || ''
                return this.requestServiceProcess(serviceId).then((res) => {
                    this.progressStatus = (res.processInfos || []).map((item) => {
                        item.icon = item.code
                        item.isLoading = item.status === 'doing'
                        return item
                    })
                    this.permission = res.opPermission || false
                    this.storeBuildInfo = res.storeBuildInfo || {}

                    this.currentStepIndex = (this.progressStatus.findIndex(x => x.status === 'doing') || 0) + 1
                    const lastStep = this.progressStatus[this.progressStatus.length - 1] || {}
                    this.isOver = lastStep.status === 'success'
                })
            },

            loopProgress () {
                this.getServiceProcess().catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' }))
                clearTimeout(this.loopProgress.timeId)
                if (!this.isOver) this.loopProgress.timeId = setTimeout(this.loopProgress, 5000)
            },

            toServiceList () {
                this.$router.push({
                    name: 'atomList',
                    params: {
                        type: 'service'
                    }
                })
            },

            toAtomStore () {
                this.$router.push({
                    name: 'atomHome',
                    query: {
                        pipeType: 'service'
                    }
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .service-progress-home {
        height: 100%;
        background: $grayBackGroundColor;
    }

    .progress-main {
        height: calc(100% - 65px);
        box-shadow: 1px 2px 3px 0px rgba(0,0,0,0.05);
        background: $white;
        padding: 32px;
        /deep/ .main-body {
            height: calc(100% - 50px);
        }
        /deep/ .main-footer {
            margin-top: 16px;
            .bk-button + .bk-button {
                margin-left: 24px;
            }
        }
    }

    .service-progress-main {
        width: 1460px;
        height: calc(100% - 116px);
        box-shadow: 1px 2px 3px 0px rgba(0,0,0,0.05);
        margin: 33px auto;
        background: $white;
        .progress-header {
            height: 65px;
            position: relative;
            border-bottom: 1px solid $lightBorder;
            .progress-steps {
                padding: 22px 180px 0;
            }
            .progress-cancle {
                position: absolute;
                right: 24px;
                top: 16px;
            }
        }
    }
</style>
