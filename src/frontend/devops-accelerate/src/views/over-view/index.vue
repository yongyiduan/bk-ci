<template>
    <article class="overview-home">
        <ul class="overview-cards">
            <li class="card g-accelerate-box" v-for="card in taskCards" :key="card.label">
                <logo :name="card.icon" size="42" class="card-logo"></logo>
                <h5>
                    <p class="g-accelerate-black-font">{{ card.num }}</p>
                    <span class="g-accelerate-gray-font">{{ card.label }}</span>
                </h5>
            </li>
        </ul>

        <section class="g-accelerate-chart-box chart">
            <header class="chart-head">
                <span class="g-accelerate-black-font">耗时分布</span>
                <bk-tab :active.sync="takeTimeDateType" type="unborder-card">
                    <bk-tab-panel v-for="(panel, index) in timeGap" v-bind="panel" :key="index"></bk-tab-panel>
                </bk-tab>
            </header>
            <canvas class="take-time" height="323"></canvas>
        </section>

        <section class="g-accelerate-chart-box chart">
            <header class="chart-head build-trend">
                <span class="g-accelerate-black-font">编译次数趋势</span>
                <bk-tab :active.sync="buildNumDateType" type="unborder-card">
                    <bk-tab-panel v-for="(panel, index) in timeGap" v-bind="panel" :key="index"></bk-tab-panel>
                </bk-tab>
            </header>
            <canvas class="build-num" height="323"></canvas>
        </section>
    </article>
</template>

<script>
    import { getOverViewStatData, getCompileNumberTrend, getTimeConsumingTrend } from '@/api'
    import BKChart from '@tencent/bkchart.js'
    import logo from '../../components/logo'

    export default {
        components: {
            logo
        },

        data () {
            return {
                taskCards: {
                    instanceNum: {
                        label: '加速方案数',
                        num: 0,
                        icon: 'acceleration-plan'
                    },
                    executeCount: {
                        label: '加速次数',
                        num: 0,
                        icon: 'accelerations-num'
                    },
                    executeTimeHour: {
                        label: '总耗时(h)',
                        num: 0,
                        icon: 'total-time'
                    },
                    savingRate: {
                        label: '节省耗时(h)',
                        num: 0,
                        icon: 'save-time'
                    }
                },
                timeGap: [
                    { name: 'week', label: '近一周', count: 10 },
                    { name: 'month', label: '近一月', count: 20 },
                    { name: 'year', label: '近一年', count: 30 }
                ],
                takeTimeDateType: 'week',
                buildNumDateType: 'week',
                takeTimeChart: {},
                buildNumChart: {}
            }
        },

        computed: {
            projectId () {
                return this.$route.params.projectId
            }
        },

        watch: {
            takeTimeDateType () {
                this.drawTakeTimeChart()
            },

            buildNumDateType () {
                this.drawBuildNum()
            }
        },

        mounted () {
            this.getSummaryCount()
            this.drawTakeTimeChart()
            this.drawBuildNum()
        },

        methods: {
            getSummaryCount () {
                getOverViewStatData(this.projectId).then((res) => {
                    this.taskCards.instanceNum.num = res.instanceNum || 0
                    this.taskCards.executeCount.num = res.executeCount || 0
                    this.taskCards.executeTimeHour.num = res.executeTimeHour || 0
                    this.taskCards.savingRate.num = res.savingRate || 0
                })
            },

            drawTakeTimeChart () {
                getTimeConsumingTrend(this.takeTimeDateType, this.projectId).then((res) => {
                    const context = document.querySelector('.take-time')
                    this.takeTimeChart = new BKChart(context, {
                        type: 'line',
                        data: {
                            labels: res.map(x => x.date),
                            datasets: [
                                {
                                    label: '未加速耗时',
                                    fill: true,
                                    backgroundColor: 'rgba(43, 124, 255,0.3)',
                                    borderColor: 'rgba(43, 124, 255,1)',
                                    lineTension: 0,
                                    borderWidth: 2,
                                    pointRadius: 0,
                                    pointHitRadius: 3,
                                    pointHoverRadius: 3,
                                    data: res.map(x => x.estimateTime)
                                },
                                {
                                    label: '实际耗时',
                                    fill: true,
                                    backgroundColor: 'rgba(0, 204, 158, 0.3)',
                                    borderColor: 'rgba(0, 204, 158, 1)',
                                    lineTension: 0,
                                    borderWidth: 2,
                                    pointRadius: 0,
                                    pointHitRadius: 3,
                                    pointHoverRadius: 3,
                                    data: res.map(x => x.executeTime)
                                }
                            ]
                        },
                        options: {
                            responsive: true,
                            legend: {
                                position: 'top',
                                legendIcon: 'arc',
                                align: 'start',
                                labels: {
                                    padding: 15
                                }
                            },
                            crosshair: {
                                enabled: true,
                                line: {
                                    color: '#3a84ff',
                                    width: 0.5
                                }
                            },
                            tooltips: {
                                mode: 'nearest',
                                intersect: false
                            },
                            scales: {
                                yAxes: {
                                    scaleLabel: {
                                        display: true,
                                        padding: 0
                                    },
                                    gridLines: {
                                        drawTicks: false,
                                        borderDash: [5, 5]
                                    },
                                    ticks: {
                                        padding: 10
                                    }
                                },
                                xAxes: {
                                    scaleLabel: {
                                        display: true,
                                        padding: 0
                                    },
                                    gridLines: {
                                        drawTicks: false,
                                        display: false
                                    },
                                    ticks: {
                                        padding: 10
                                    }
                                }
                            }
                        }
                    })
                })
            },

            drawBuildNum () {
                getCompileNumberTrend(this.buildNumDateType, this.projectId).then((res = []) => {
                    const context = document.querySelector('.build-num')
                    this.buildNumChart = new BKChart(context, {
                        type: 'line',
                        data: {
                            labels: res.map(x => x.date),
                            datasets: [
                                {
                                    label: '编译次数',
                                    fill: false,
                                    backgroundColor: 'rgba(43, 124, 255,0.3)',
                                    borderColor: 'rgba(43, 124, 255,1)',
                                    lineTension: 0,
                                    borderWidth: 2,
                                    pointRadius: 1.5,
                                    pointHitRadius: 3,
                                    pointHoverRadius: 3,
                                    data: res.map(x => x.executeCount)
                                }
                            ]
                        },
                        options: {
                            responsive: true,
                            legend: {
                                display: false
                            },
                            crosshair: {
                                enabled: true,
                                line: {
                                    color: '#3a84ff',
                                    width: 0.5
                                }
                            },
                            tooltips: {
                                mode: 'nearest',
                                intersect: false
                            },
                            scales: {
                                yAxes: {
                                    scaleLabel: {
                                        display: true,
                                        padding: 0
                                    },
                                    gridLines: {
                                        drawTicks: false,
                                        borderDash: [5, 5]
                                    },
                                    ticks: {
                                        padding: 10
                                    }
                                },
                                xAxes: {
                                    scaleLabel: {
                                        display: true,
                                        padding: 0
                                    },
                                    gridLines: {
                                        drawTicks: false,
                                        display: false
                                    },
                                    ticks: {
                                        padding: 10
                                    }
                                }
                            }
                        }
                    })
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    .overview-home {
        padding: .2rem;
        margin: 0 auto;
    }
    .overview-cards {
        width: 100%;
        margin-bottom: 16.94px;
        &::after {
            content: '';
            display: table;
            clear: both;
        }
        .card {
            float: left;
            width: calc(25% - .075rem);
            height: 84.7px;
            margin-right: .1rem;
            display: flex;
            align-items: center;
            .card-logo {
                border-radius: 100px;
                background: #e1ecff;
                margin: 0 16px 0 30px;
            }
            .g-accelerate-black-font {
                font-size: 24px;
                line-height: 32px;
            }
            .g-accelerate-gray-font {
                font-size: 12px;
                line-height: 18px;
                font-weight: normal;
            }
            &:last-child {
                margin: 0;
            }
        }
    }
    .chart {
        margin-bottom: 10.59px;
        height: 360px;
        .take-time, .build-num {
            height: 323px;
            width: 100%;
        }
        &:last-child {
            margin-bottom: 0;
        }
    }
    .build-trend {
        margin-bottom: 10px;
    }
    .chart-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 22px;
        /deep/ .bk-tab-header {
            background-color: #fff;
            height: 20px;
            line-height: 20px;
            background-image: none;
            .bk-tab-label-wrapper .bk-tab-label-list {
                height: 20px;
                .bk-tab-label-item {
                    line-height: 20px;
                    color: #63656e;
                    min-width: 36px;
                    padding: 0;
                    margin-right: 16px;
                    &:last-child {
                        margin: 0;
                    }
                    &::after {
                        height: 2px;
                        left: 0px;
                        width: 100%;
                    }
                    &.active {
                        color: #3a84ff;
                    }
                    .bk-tab-label {
                        font-size: 12px;
                    }
                }
            }
            .bk-tab-header-setting {
                height: 20px;
                line-height: 20px;
            }
        }
        /deep/ .bk-tab-section {
            padding: 0;
        }
    }
</style>
