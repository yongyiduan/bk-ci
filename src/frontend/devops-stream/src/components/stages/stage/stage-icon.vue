<template>
    <span :class="['stage-icon', { 'no-txt': !stageTxt }, stageStatus]" v-bk-tooltips="stageToolTip">
        <icon
            :name="reviewStatausIcon"
            size="28"
            class="review-icon"
        />
        <span class="stage-txt">{{ stageTxt }}</span>
    </span>
</template>

<script>
    export default {
        props: {
            stageCheck: Object
        },

        computed: {
            stageTxt () {
                const textMap = {
                    QUALITY_CHECK_FAIL: '质量红线',
                    QUALITY_CHECK_PASS: '质量红线',
                    QUALITY_CHECK_WAIT: '质量红线',
                    REVIEW_PROCESSED: '人工审核',
                    REVIEW_ABORT: '人工审核',
                    REVIEWING: '人工审核'
                }
                let txt = textMap[this.stageCheck?.status]
                if (!txt) {
                    if (this.stageCheck?.manualTrigger) {
                        txt = '人工审核'
                    }

                    if (this.hasRuleId) {
                        txt = '质量红线'
                    }
                }
                return txt
            },

            stageStatus () {
                const textMap = {
                    QUALITY_CHECK_FAIL: 'fail',
                    QUALITY_CHECK_PASS: 'success',
                    QUALITY_CHECK_WAIT: 'wait',
                    REVIEW_PROCESSED: 'success',
                    REVIEW_ABORT: 'fail',
                    REVIEWING: 'wait'
                }
                return textMap[this.stageCheck?.status]
            },

            reviewStatausIcon () {
                try {
                    if (this.stageCheck?.isReviewError) return 'review-error'
                    switch (true) {
                        case this.stageCheck?.status === 'REVIEWING':
                            return 'reviewing'
                        case this.stageCheck?.status === 'QUEUE':
                            return 'review-waiting'
                        case this.stageCheck?.status === 'REVIEW_PROCESSED':
                            return 'check-fill'
                        case this.stageCheck?.status === 'QUALITY_CHECK_FAIL':
                            return 'error-fill'
                        case this.stageCheck?.status === 'QUALITY_CHECK_PASS':
                            return 'check-fill'
                        case this.stageCheck?.status === 'QUALITY_CHECK_WAIT':
                            return 'reviewing'
                        case this.stageCheck?.status === 'REVIEW_ABORT':
                            return 'error-fill'
                        case this.stageStatusCls === 'SKIP':
                        case this.stageCheck?.status === undefined:
                            return this.stageCheck?.manualTrigger || this.hasRuleId ? 'waiting-fill' : 'review-auto-gray'
                        case this.stageCheck?.status:
                            return 'review-auto-pass'
                        default:
                            return this.stageCheck?.manualTrigger ? 'review-enable' : 'review-auto'
                    }
                } catch (e) {
                    console.warn('get review icon error: ', e)
                    return 'review-auto'
                }
            },

            hasRuleId () {
                return Array.isArray(this.stageCheck?.ruleIds) && this.stageCheck?.ruleIds.length > 0
            },

            stageToolTip () {
                const contentMap = {
                    REVIEWING: '等待审核',
                    QUALITY_CHECK_FAIL: '质量红线已拦截',
                    QUALITY_CHECK_WAIT: '等待质量红线审核'
                }
                return {
                    content: contentMap[this.stageCheck?.status],
                    disabled: !['REVIEWING', 'QUALITY_CHECK_WAIT'].includes(this.stageCheck?.status)
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .stage-icon {
        line-height: 26px;
        border-radius: 100px;
        border: 1px solid #d0d8ea;
        display: flex;
        align-items: center;
        background: #fff;
        font-size: 12px;
        &.no-txt {
            .stage-txt {
                padding-right: 0px;
            }
        }
        &.fail {
            color: #FF5656;
            border-color: #FF5656;
            &:hover {
                background: #FFEEEE;
            }
        }
        &.success {
            color: #2DCB56;
            border-color: #2DCB56;
            &:hover {
                background: #EAFFEE;
            }
        }
        &.wait {
            color: #3C96FF;
            border-color: #3C96FF;
            &:hover {
                background: #ECF5FF;
            }
        }
    }
    .stage-txt {
        padding-right: 10px;
    }
</style>
