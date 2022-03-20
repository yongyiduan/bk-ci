<template>
    <section class="stage-home">
        <stage-icon
            class="review-icon"
            v-if="showStageCheck(stage.checkIn)"
            :stage-check="stage.checkIn"
            @click.native="handleIconClick('checkIn')"
        />

        <stage-icon
            class="review-icon check-out"
            v-if="showStageCheck(stage.checkOut)"
            :stage-check="stage.checkOut"
            @click.native="handleIconClick('checkOut')"
        />

        <h3 class="stage-title">
            <span :class="`${stageStatusCls} title-content`">
                <i :class="stageStatusIcon"></i>
                <span class="stage-name text-ellipsis" v-bk-overflow-tips>{{ stage.name }}</span>
            </span>
            <span v-if="['FAILED', 'CANCELED'].includes(stage.status)"
                v-bk-tooltips="computedOptToolTip"
                :class="['stage-retry', { disabled: !curPipeline.enabled || !permission }]"
                @click="retry"
            >Re-run</span>
            <span class="stage-connector" v-if="stageIndex < stageNum - 1">
                <i class="bk-icon icon-right-shape connector-angle"></i>
            </span>
        </h3>

        <job
            v-for="(job, jobIndex) in stage.containers"
            :job="job"
            :key="job.id"
            :job-index="jobIndex"
            :stage-index="stageIndex"
            :stage-num="stageNum"
            :stages="stages"
            class="job-container"
        ></job>

        <bk-dialog v-model="showRetryStageDialog"
            render-directive="if"
            ext-cls="stage-retry-dialog"
            ok-text="Confirm"
            cancel-text="Cancle"
            :width="400"
            :auto-close="false"
            :loading="isRetrying"
            @confirm="confirmRetry"
        >
            <bk-radio-group v-model="failedContainer">
                <bk-radio :value="false">Re-run all jobs</bk-radio>
                <bk-radio :value="true">Re-run all failed jobs</bk-radio>
            </bk-radio-group>
        </bk-dialog>
    </section>
</template>

<script>
    import { mapState, mapActions } from 'vuex'
    import { getPipelineStatusClass, getPipelineStatusIconCls } from '@/components/status'
    import { pipelines } from '@/http'
    import job from '../job/index'
    import stageIcon from './stage-icon.vue'

    export default {
        components: {
            job,
            stageIcon
        },

        props: {
            stages: Array,
            stage: Object,
            stageIndex: Number,
            stageNum: Number
        },

        data () {
            return {
                showRetryStageDialog: false,
                failedContainer: false,
                isRetrying: false
            }
        },

        computed: {
            ...mapState(['projectId', 'permission', 'showStageReviewPanel', 'curPipeline']),

            computedOptToolTip () {
                return {
                    content: !this.curPipeline.enabled ? 'Pipeline disabled' : 'Permission denied',
                    disabled: this.curPipeline.enabled && this.permission
                }
            },

            stageStatusCls () {
                return getPipelineStatusClass(this.stage.status, this.stage?.stageControlOption?.enable === false)
            },

            stageStatusIcon () {
                return getPipelineStatusIconCls(this.stage.status)
            },

            isFinallyStage () {
                return this.stage.finally === true
            }
        },

        mounted () {
            this.autoOpenReview()
        },

        methods: {
            ...mapActions([
                'toggleStageReviewPanel'
            ]),

            autoOpenReview () {
                const query = this.$route.query || {}
                const checkIn = query.checkIn
                const checkOut = query.checkOut
                if (checkIn === this.stage.id) {
                    this.handleIconClick('checkIn')
                } else if (checkOut === this.stage.id) {
                    this.handleIconClick('checkOut')
                }
            },

            handleIconClick (type) {
                this.toggleStageReviewPanel({
                    isShow: true,
                    type,
                    stage: this.stage
                })
            },

            showStageCheck (stageControl = {}) {
                const hasReviewFlow = stageControl.manualTrigger
                const hasReviewQuality = stageControl.ruleIds && stageControl.ruleIds.length > 0
                return !this.isFinallyStage && (hasReviewFlow || hasReviewQuality)
            },

            retry () {
                if (!this.curPipeline.enabled || !this.permission) return

                this.showRetryStageDialog = true
            },

            confirmRetry () {
                this.isRetrying = true
                const routeParams = this.$route.params || {}
                const query = {
                    taskId: this.stage.id,
                    failedContainer: this.failedContainer
                }
                pipelines.rebuildPipeline(this.projectId, routeParams.pipelineId, routeParams.buildId, query).then(() => {
                    this.showRetryStageDialog = false
                    this.$bkMessage({ theme: 'success', message: 'Re-run successful' })
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isRetrying = false
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .stage-home {
        position: relative;
        min-width: 280px;
        border-radius: 2px;
        padding: 0 0 24px 0;
        background: #f5f5f5;
        margin: 0 80px 0 0;
        .review-icon {
            position: absolute;
            cursor: pointer;
            top: 11px;
            left: -14px;
            z-index: 3;
            &.check-out {
                right: -14px;
                left: auto;
            }
        }
    }
    .stage-title {
        position: relative;
        width: 100%;
        height: 50px;
        line-height: 50px;
        border-radius: 2px;
        text-align: center;
        .title-content {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f3f3f3;
            border: 1px solid #d0d8ea;
            color: black;
            .stage-name {
                max-width: 95px;
                margin-left: 5px;
                display: inline-block;
            }
            .bk-icon {
                font-size: 22px;
            }
            &.skip {
                color: #c3cdd7;
                fill: #c3cdd7;
                .stage-name {
                    color: #c3cdd7;
                    text-decoration: line-through;
                }
            }
            &.running {
                background-color: #eff5ff;
                border-color: #d4e8ff;
                color: #3c96ff;
            }
            &.warning {
                background-color: #f3f3f3;
                border-color: #d0d8ea;
                color: black;
            }

            &.danger {
                border-color: #ffd4d4;
                background-color: #fff9f9;
                color: black;
                .bk-icon {
                    color: #ff5656;
                }
            }
            &.success {
                background-color: #f3fff6;
                border-color: #bbefc9;
                color: black;
                .bk-icon {
                    color: #34da7b;
                }
            }
        }
        .stage-retry {
            cursor: pointer;
            position: absolute;
            right: 6%;
            top: 18px;
            line-height: 14px;
            color: $primaryColor;
            &.disabled {
                cursor: not-allowed;
                color: #c4c6cc;
            }
        }
    }
    .stage-connector {
        position: absolute;
        width: 80px;
        height: 2px;
        right: -81px;
        top: 24px;
        background: #c3cdd7;
        color: #c3cdd7;
        &:before {
            content: '';
            width: 5px;
            height: 10px;
            position: absolute;
            left: -1px;
            top: -4px;
            background-color: #c3cdd7;
            border-radius: 0 100px 100px 0;
        }
        .connector-angle {
            position: absolute;
            right: -2px;
            top: -6px;
        }
    }
    .stage-retry-dialog {
        .bk-form-radio {
            display: block;
            margin-top: 15px;
            .bk-radio-text {
                font-size: 14px;
            }
        }
    }
    .job-container {
        padding: 0 20px;
    }
</style>
