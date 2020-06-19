<template>
    <article class="service-overview-wrapper">
        <header class="inner-header">
            <span class="title"> {{ $t('store.微扩展概览') }} </span>
        </header>

        <main class="service-overview-container">
            <section class="service-static">
                <section class="flex74 overview-section">
                    <h3> {{ $t('store.总体统计') }} </h3>
                    <section class="section-static">
                        <h3>
                            <p style="border-color: #7ed321"><span style="color: #7ed321">{{staticData.downloads}}</span></p>
                            <span class="static-title"> {{ $t('store.安装量') }} </span>
                        </h3>
                        <h3>
                            <p style="border-color: #2680eb"><span style="color: #2680eb">{{staticData.commentCnt}}</span></p>
                            <span class="static-title"> {{ $t('store.评论数') }} </span>
                        </h3>
                        <h3>
                            <p style="border-color: #2680eb"><span style="color: #2680eb">{{staticData.score}}</span></p>
                            <span class="static-title"> {{ $t('store.星级') }} </span>
                        </h3>
                    </section>
                </section>
                <section class="flex32 overview-section">
                    <h3> {{ $t('store.微扩展代码') }} </h3>
                    <section class="section-code">
                        <h3>
                            <span class="code-title"> {{ $t('store.已托管至') }} </span>：<span>工蜂GIT</span>
                        </h3>
                        <h3>
                            <span class="code-title"> {{ $t('store.代码库') }} </span>：<span class="code-url">{{ currentService.codeSrc }}</span>
                            <span class="ml4 click-txt" @click="copyCodeUrl">{{ $t('store.复制') }}</span>
                        </h3>
                        <h3>
                            <span class="code-title"> {{ $t('store.授权人') }} </span>：<span>{{ currentService.repositoryAuthorizer }}</span>
                            <bk-popover placement="top">
                                <i class="devops-icon icon-info-circle ml4"></i>
                                <template slot="content">
                                    <p> {{ $t('store.在发布微扩展时，使用授权人的身份拉取微扩展代码自动构建打包，或设置微扩展可见范围') }} </p>
                                </template>
                            </bk-popover>
                            <span class="ml4 click-txt"
                                v-if="userInfo.isProjectAdmin && userInfo.userName !== currentService.repositoryAuthorizer"
                                @click="modifyRepoMemInfo"
                            >{{ $t('store.重置授权') }}</span>
                        </h3>
                    </section>
                </section>
            </section>

            <section class="service-map">
                <section class="flex74 overview-section">
                    <h3> {{ $t('store.趋势图') }} </h3>
                    <!-- <bk-tab :active.sync="active" class="custom-tabs">
                        <template slot="setting">
                            <bk-select value="最近30天" class="trend-date" :clearable="false">
                                <bk-option v-for="option in trendDate"
                                    :key="option.name"
                                    :id="option.name"
                                    :name="option.name">
                                </bk-option>
                            </bk-select>
                        </template>
                        <bk-tab-panel name="installNum" :label="$t('store.安装量')">
                            <section class="install-line"></section>
                        </bk-tab-panel>

                        <bk-tab-panel name="uninstallReason" :label="$t('store.卸载原因占比')">
                            <section class="uninstall-pie"></section>
                        </bk-tab-panel>
                    </bk-tab> -->
                    <section class="custom-tabs">
                        <img src="../../images/building.png">
                        <p>{{ $t('store.功能正在建设中') }}···</p>
                    </section>
                </section>

                <section class="flex32 overview-section">
                    <h3> {{ $t('store.最新动态') }} </h3>
                    <section class="overview-news" v-bkloading="{ isLoading }">
                        <bk-timeline :list="list"></bk-timeline>
                    </section>
                </section>
            </section>
        </main>
    </article>
</template>

<script>
    import { mapGetters } from 'vuex'

    const echarts = require('echarts/lib/echarts')
    require('echarts/lib/chart/line')
    require('echarts/lib/chart/bar')
    require('echarts/lib/component/tooltip')

    export default {
        data () {
            return {
                trendDate: [
                    { name: '最近30天' },
                    { name: '最近半年' },
                    { name: '最近一年' }
                ],
                list: [],
                isLoading: false,
                staticData: {
                    downloads: 0,
                    commentCnt: 0,
                    score: 0
                }
            }
        },

        computed: {
            ...mapGetters('store', {
                'currentService': 'getCurrentService',
                'userInfo': 'getUserInfo'
            })
        },

        mounted () {
            this.initData()
        },

        methods: {
            initEchart () {
                const lineEle = document.querySelector('.install-line')
                const chartLine = echarts.init(lineEle)
                chartLine.setOption({
                    xAxis: {
                        boundaryGap: false,
                        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                        splitLine: {
                            lineStyle: {
                                type: 'dashed'
                            },
                            show: true
                        },
                        axisLabel: {
                            color: '#737987'
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            lineStyle: {
                                color: '737987'
                            }
                        }
                    },
                    yAxis: {
                        type: 'value',
                        splitLine: {
                            lineStyle: {
                                type: 'dotted'
                            },
                            show: true
                        },
                        axisLabel: {
                            color: '#737987'
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            lineStyle: {
                                color: '737987'
                            }
                        }
                    },
                    tooltip: {
                        show: true
                    },
                    series: [{
                        data: [820, 932, 901, 934, 1290, 1330, 1320],
                        type: 'line',
                        smooth: true
                    }],
                    grid: {
                        x: 60,
                        y: 30,
                        x2: 30,
                        y2: 30,
                        borderWidth: 1
                    }
                })

                const barEle = document.querySelector('.uninstall-pie')
                barEle.style.width = lineEle.offsetWidth + 'px'
                barEle.style.height = lineEle.offsetHeight + 'px'
                const chartBar = echarts.init(barEle)
                chartBar.setOption({
                    xAxis: {
                        type: 'category',
                        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                        axisLabel: {
                            color: '#737987'
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            lineStyle: {
                                color: '737987'
                            }
                        }
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: {
                            color: '#737987'
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            lineStyle: {
                                color: '737987'
                            }
                        }
                    },
                    tooltip: {
                        show: true
                    },
                    grid: {
                        x: 60,
                        y: 30,
                        x2: 30,
                        y2: 30,
                        borderWidth: 1
                    },
                    series: [{
                        data: [120, 200, 150, 80, 70, 110, 130],
                        barWidth: '30%',
                        type: 'bar'
                    }]
                })
            },

            initData () {
                this.isLoading = true
                const serviceCode = this.currentService.serviceCode
                Promise.all([
                    this.$store.dispatch('store/requestVersionLog', serviceCode),
                    this.$store.dispatch('store/requestServiceStic', serviceCode)
                ]).then(([logRes, staticRes]) => {
                    this.staticData = staticRes || {}
                    const records = logRes.records || []
                    this.list = records.map((x) => ({
                        tag: x.createTime,
                        content: `${x.creator} ${this.$t('store.新增版本')}${x.version}`
                    }))
                }).catch(err => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => (this.isLoading = false))
            },

            copyCodeUrl () {
                const input = document.createElement('input')
                document.body.appendChild(input)
                input.setAttribute('value', this.currentService.codeSrc)
                input.select()
                if (document.execCommand('copy')) {
                    document.execCommand('copy')
                    this.$bkMessage({ theme: 'success', message: this.$t('store.复制代码库地址成功') })
                }
                document.body.removeChild(input)
            },

            modifyRepoMemInfo () {
                const serviceCode = this.currentService.serviceCode
                const projectCode = this.currentService.projectCode
                this.$store.dispatch('store/checkIsOAuth').then((res) => {
                    if (res.status === 403) {
                        window.open(res.url, '_self')
                        return
                    }

                    return this.$store.dispatch('store/resetServiceGit', { serviceCode, projectCode }).then((res) => {
                        if (res) {
                            this.currentService.repositoryAuthorizer = this.userInfo.userName
                            this.$store.dispatch('store/updateCurrentService', { res: this.currentService })
                            this.$bkMessage({ message: this.$t('this.重置授权成功'), theme: 'success', limit: 1 })
                        }
                    })
                }).catch(err => this.$bkMessage({ message: err.message || err, theme: 'error' }))
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import './../../assets/scss/conf';

    .overview-news {
        padding: 21px;
        overflow: auto;
    }

    .install-line, .uninstall-pie {
        height: 100%;
        width: 100%;
    }

    .custom-tabs {
        margin-top: 8px;
        height: calc(100% - 19px);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        /deep/ .bk-tab-section {
            height: calc(100% - 43px);
            background: $white;
            padding: 0;
            .bk-tab-content {
                height: 100%;
                width: 100%;
            }
        }
        .trend-date {
            width: 130px;
            height: 32px;
            margin: 5px 5px 0 0;
        }
    }
    
    .section-code {
        display: flex;
        flex-direction: column;
        justify-content: space-around;
        padding: 10px 0;
        > h3 {
            display: flex;
            align-items: center;
            font-weight: normal;
            font-size: 14px;
            color: $fontWeightColor;
        }
        .code-title {
            display: inline-block;
            width: 80px;
            text-align: right;
        }
        .code-url {
            max-width: 14vw;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .click-txt {
            display: inline-block;
            color: $primaryColor;
            cursor: pointer;
            font-size: 12px;
        }
        .ml4 {
            margin-left: 4px;
        }
    }

    .section-static {
        display: flex;
        flex-direction: row;
        justify-content: space-around;
        align-items: center;
        text-align: center;
        h3 {
            height: 100%;
            flex: 1;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
        }
        .static-title {
            font-size: 12px;
            color: $fontWeightColor;
        }
        p {
            width: 20%;
            height: 0;
            padding-bottom: 20%;
            border-radius: 100%;
            border: 1px solid;
            box-sizing: content-box;
            margin-bottom: 7px;
            span {
                display: inline-block;
                margin-top: 50%;
                transform: translateY(-50%);
                font-size: 20px;
            }
        }
    }
    .service-overview-wrapper {
        overflow: hidden;
        height: 100%;
        width: 100%;
        .inner-header {
            display: flex;
            justify-content: space-between;
            padding: 0 20px;
            width: 100%;
            height: 60px;
            border-bottom: 1px solid $borderWeightColor;
            background-color: #fff;
            box-shadow:0px 2px 5px 0px rgba(51,60,72,0.03);
            .title {
                font-size: 16px;
                line-height: 59px;
            }
        }
        .service-overview-container {
            height: calc(100% - 60px);
            width: 100%;
            padding: 2.3% 1.7%;
            overflow: auto;
        }
        .service-static {
            height: 26.3%;
            display: flex;
            flex-direction: row;
            > section:nth-child(2) {
                margin-left: 1.7%;
            }
        }
        .service-map {
            margin-top: 2.3%;
            height: 65.8%;
            display: flex;
            flex-direction: row;
            > section:nth-child(2) {
                margin-left: 1.7%;
            }
        }
        .overview-section {
            h3 {
                line-height: 19px;
                font-size: 14px;
                color: $fontWeightColor;
            }
            > section {
                margin-top: 8px;
                height: calc(100% - 19px);
                background: #fff;
                border: 1px solid rgba(221,228,235,1);
                border-radius: 2px 2px 2px 2px;
            }
        }
        .flex74 {
            flex: 74;
        }
        .flex32 {
            flex: 32;
        }
    }
</style>
