<template>
    <section v-if="(stageControl.ruleIds || []).length">
        <span class="review-title">Quality Gates</span>
        <section class="review-quality">
            <bk-collapse v-model="activeName">
                <bk-collapse-item v-for="(qualityItem, index) in qualityList" :key="index" :name="qualityItem.hashId">
                    <span class="quality-summary">
                        <span class="quality-name text-ellipsis" v-bk-overflow-tips>{{ qualityItem.ruleName }}</span>
                        <span :class="qualityItem.interceptResult === 'PASS' ? 'success' : 'error'">
                            {{ qualityItem.interceptResult | interceptFilter }} ({{ qualityItem.interceptList.length }})
                        </span>
                    </span>
                    <section slot="content">
                        <!-- <h5 class="quality-title">Rules</h5> -->
                        <ul>
                            <li class="quality-content text-ellipsis" v-for="(intercept, interceptIndex) in qualityItem.interceptList" :key="interceptIndex">
                                <span :class="{ 'quality-icon': true, 'success': intercept.pass }">
                                    <i :class="`bk-icon ${ intercept.pass ? 'icon-check-1' : 'icon-close' }`"></i>
                                </span>
                                <span class="text-ellipsis mr5" v-bk-overflow-tips>{{ intercept | nameFilter }}</span>
                                <i class="bk-icon icon-info" v-bk-tooltips="{ content: intercept.logPrompt }" v-if="intercept.logPrompt"></i>
                            </li>
                        </ul>
                    </section>
                </bk-collapse-item>
            </bk-collapse>
        </section>
        <bk-divider class="review-quality-divider"></bk-divider>
    </section>
</template>

<script>
    import { mapState } from 'vuex'
    import { pipelines } from '@/http'

    export default {
        filters: {
            interceptFilter (val) {
                const resultMap = {
                    PASS: 'Passed',
                    FAIL: 'Blocked'
                }
                return resultMap[val]
            },

            nameFilter (intercept) {
                const { indicatorName, operation, actualValue, value } = intercept
                const operationMap = {
                    GT: '>',
                    GE: '>=',
                    LT: '<',
                    LE: '<=',
                    EQ: '='
                }
                return `${indicatorName}当前值(${actualValue || 'null'})，期望${operationMap[operation]}${value || 'null'}`
            }
        },

        props: {
            stageControl: {
                type: Object,
                default: () => ({})
            }
        },

        data () {
            return {
                activeName: [],
                isLoading: false,
                qualityList: []
            }
        },

        computed: {
            ...mapState(['projectId'])

        },

        created () {
            this.requestQualityLineFromApi()
        },

        methods: {
            requestQualityLineFromApi () {
                if ((this.stageControl.ruleIds || []).length <= 0) return

                const params = [
                    this.projectId,
                    this.$route.params.pipelineId,
                    this.$route.params.buildId,
                    this.stageControl.ruleIds,
                    this.stageControl.checkTimes || 1
                ]

                this.isLoading = true
                pipelines.requestQualityGate(...params).then((res) => {
                    this.qualityList = res
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
    .review-title {
        color: #666770;
        line-height: 20px;
        padding-left: 7px;
        position: relative;
        display: block;
        font-size: 14px;
        font-weight: bold;
        &:before {
            content: '';
            position: absolute;
            width: 2px;
            height: 16px;
            background: #3a84ff;
            left: 0;
            top: 2px;
        }
    }
    .review-quality {
        margin-top: 16px;
        /deep/ .bk-collapse {
            display: flex;
            flex-wrap: wrap;
            font-size: 12px;
            .bk-collapse-item {
                width: 50%;
                margin-bottom: 12px;
                .bk-collapse-item-header {
                    line-height: 20px;
                    height: 20px;
                    font-size: 12px;
                }
            }
            .quality-title {
                margin-top: 8px;
                color: #666770;
            }
            .quality-content {
                margin-top: 6px;
                color: #666770;
                display: flex;
                align-items: center;
            }
        }
        .quality-summary {
            display: flex;
            align-items: center;
            .quality-name {
                margin-right: 12px;
                max-width: 270px;
            }
        }
        .quality-icon {
            width: 14px;
            height: 14px;
            border-radius: 100%;
            background: #ffdddd;
            box-sizing: border-box;
            display: inline-block;
            line-height: 12px;
            text-align: center;
            margin-right: 6px;
            &.success {
                background: #e5f6ea;
            }
            .bk-icon {
                font-size: 14px;
            }
        }
        .icon-close, .error {
            color: #ea3636;
        }
        .icon-check-1, .success {
            color: #3fc06d;
        }
    }
    .review-quality-divider {
        margin-bottom: 24px !important;
        margin-top: 12px !important;
    }
</style>
