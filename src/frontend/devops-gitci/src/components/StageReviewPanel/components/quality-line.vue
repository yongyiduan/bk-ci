<template>
    <section v-if="(stageControl.ruleIds || []).length">
        <span class="review-title">质量红线</span>
        <section class="review-quality">
            <bk-collapse v-model="activeName">
                <bk-collapse-item :name="index" v-for="(qualityItem, index) in qualityList" :key="index">
                    <span class="quality-summary">
                        <span class="quality-name">{{ qualityItem.ruleName }}</span>
                        <span :class="{ 'quality-icon': true, 'success': qualityItem.interceptResult === 'PASS' }">
                            <i :class="`bk-icon ${ qualityItem.interceptResult === 'PASS' ? 'icon-check-1' : 'icon-close' }`"></i>
                        </span>
                        <span>{{ qualityItem.interceptResult | interceptFilter }}（{{ qualityItem.interceptList.length }}）</span>
                    </span>
                    <section slot="content">
                        <h5 class="quality-title">规则</h5>
                        <ul>
                            <li class="quality-content" v-for="(intercept, interceptIndex) in qualityItem.interceptList" :key="interceptIndex">
                                <span :class="{ 'quality-icon': true, 'success': intercept.pass }">
                                    <i :class="`bk-icon ${ intercept.pass ? 'icon-check-1' : 'icon-close' }`"></i>
                                </span>
                                <span>{{ intercept.detail }}</span>
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
                    PASS: '已通过',
                    FAIL: '已拦截'
                }
                return resultMap[val]
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
                activeName: -1,
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
                    this.stageControl.ruleIds
                ]

                this.isLoading = true
                pipelines.requestQualityLine(...params).then((res) => {
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
            font-size: 14px;
            .bk-collapse-item {
                width: 50%;
                margin-bottom: 12px;
                .bk-collapse-item-header {
                    line-height: 20px;
                    height: 20px;
                }
            }
            .quality-title {
                margin-top: 8px;
                color: #666770;
            }
            .quality-content {
                margin-top: 4px;
                color: #666770;
            }
        }
        .quality-summary {
            display: flex;
            align-items: center;
            .quality-icon {
                margin: 0 5.2px 0 9.3px;
                .bk-icon {
                    margin-left: 0px;
                }
            }
        }
        .quality-icon {
            width: 16px;
            height: 16px;
            border-radius: 100%;
            background: #ffdddd;
            box-sizing: border-box;
            display: inline-block;
            line-height: 14px;
            text-align: center;
            margin-right: 4px;
            &.success {
                background: #e5f6ea;
            }
            .bk-icon {
                margin-left: 1px;
                font-size: 14px;
            }
            .icon-close {
                color: #ea3636;
            }
            .icon-check-1 {
                color: #3fc06d;
            }
        }
    }
    .review-quality-divider {
        margin-bottom: 24px !important;
        margin-top: 12px !important;
    }
</style>
