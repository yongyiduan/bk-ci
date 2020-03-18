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
            <a class="title-work" target="_blank" href="https://iwiki.oa.tencent.com/pages/viewpage.action?pageId=103523086"> {{ $t('store.扩展指引') }} </a>
        </h3>
        <article v-if="!isLoading" class="service-progress-main">
            <header class="progress-header">
                <bk-steps ext-cls="progress-steps" :status="currentStepStatus" :steps="progressStatus" :cur-step="currentStepIndex"></bk-steps>
                <!--bk-button class="progress-detail" @click="showDetail = true"> {{ $t('store.查看扩展详情') }} </bk-button-->
                <bk-button class="progress-cancle" @click="cancelRelease"> {{ $t('store.中止发布') }} </bk-button>
            </header>

            <section class="progress-main">
                <component :is="currentStep.code" :store-build-info="storeBuildInfo" :current-step="currentStep" :detail="serviceDetail" @freshProgress="getServiceProcess"></component>
            </section>
        </article>

        <bk-sideslider :is-show.sync="showDetail"
            :title="$t('store.查看扩展详情')"
            :quick-close="true"
            :width="640"
        >
            <template slot="content">
                <h3>
                    <img :src="serviceDetail.logoUrl">
                    <span class="lh30">{{serviceDetail.serviceName}}</span>
                </h3>
                <bk-form label-width="100" :model="serviceDetail">
                    <bk-form-item :label="$t('store.扩展标识')" property="serviceCode">
                        <span class="lh30">{{serviceDetail.serviceCode}}</span>
                    </bk-form-item>
                    <bk-form-item :label="$t('store.标签')" property="serviceCode">
                        <ul>
                            <li v-for="(label, index) in serviceDetail.labelList" :key="index">{{label}}</li>
                        </ul>
                    </bk-form-item>
                    <bk-form-item :label="$t('store.标签')" property="serviceCode">
                        <span class="lh30">{{serviceDetail.serviceCode}}</span>
                    </bk-form-item>
                </bk-form>
            </template>
        </bk-sideslider>
    </article>
</template>

<script>
    import { mapActions } from 'vuex'
    import build from '../components/common/progressSteps/build/index'
    import test from '../components/common/progressSteps/test'
    import commit from '../components/common/progressSteps/commit'
    import approve from '../components/common/progressSteps/approve'
    import begin from '../components/common/progressSteps/begin'
    import end from '../components/common/progressSteps/end'

    export default {
        components: {
            build,
            test,
            commit,
            approve,
            begin,
            end
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
                currentPipelineId: '',
                showDetail: false
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
            },

            currentStepStatus () {
                let res
                const status = (this.currentStep || {}).status || ''
                switch (status) {
                    case 'fail':
                        res = 'error'
                        break
                    case 'doing':
                        res = 'loading'
                        break
                }
                return res
            }
        },

        created () {
            this.initData()
            this.loopProgress()
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
                    this.$bkMessage({ message: this.$t('store.发起重新构建成功'), theme: 'success' })
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
                const iconMap = {
                    begin: 'order-shape',
                    build: 'panels',
                    test: 'script-files',
                    commit: 'edit2',
                    approve: 'panel-permission',
                    end: 'check-1'
                }

                return this.requestServiceProcess(serviceId).then((res) => {
                    this.progressStatus = (res.processInfos || []).map((item) => ({
                        title: item.name,
                        icon: iconMap[item.code],
                        status: item.status,
                        code: item.code
                    }))
                    this.permission = res.opPermission || false
                    this.storeBuildInfo = res.storeBuildInfo || {}
                    let index = this.progressStatus.findIndex(x => ['doing', 'fail'].includes(x.status))
                    if (index < 0) index = 0
                    this.currentStepIndex = index + 1
                    const lastStep = this.progressStatus[this.progressStatus.length - 1] || {}
                    this.isOver = lastStep.status === 'success'
                    if (this.isOver) this.currentStepIndex = this.progressStatus.length
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
                padding: 22px 280px 0 180px;
                /deep/ .icon-panels, /deep/ .icon-order-shape, /deep/ .icon-script-files, /deep/ .icon-panel-permission {
                    font-size: 14px;
                }
            }
            .progress-detail {
                position: absolute;
                right: 124px;
                top: 16px;
            }
            .progress-cancle {
                position: absolute;
                right: 24px;
                top: 16px;
            }
        }
    }
</style>
