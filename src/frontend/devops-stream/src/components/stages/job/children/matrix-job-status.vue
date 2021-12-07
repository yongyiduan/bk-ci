<template>
    <span :class="`matrix-job-status ${job.status}`">
        <status-icon :status-container="job" class="status-icon"></status-icon>
        <span class="matrix-head-percent-main">{{ getMatrixStatusTxt() }}</span>
    </span>
</template>

<script>
    import statusIcon from '@/components/stages/children/status-icon.vue'

    export default {
        components: {
            statusIcon
        },

        props: {
            job: Object
        },

        methods: {
            getMatrixStatusTxt () {
                const statusTxtMap = {
                    RUNNING: `进度${this.getPercent()}（${this.job.matrixControlOption.finishCount}/${this.job.matrixControlOption.totalCount}）`,
                    SUCCEED: '执行完毕',
                    FAILED: '执行失败',
                    SKIP: '跳过执行',
                    TERMINATE: '执行中止'
                }
                return statusTxtMap[this.job.status]
            },

            getPercent () {
                const value = this.job.matrixControlOption.finishCount * 100 / (this.job.matrixControlOption.totalCount || 1)
                return Number(value.toString().match(/^\d+(?:\.\d{0,2})?/)) + '%'
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .matrix-job-status {
        font-size: 12px;
        display: flex;
        align-items: center;
        cursor: pointer;
        .status-icon {
            width: 25px;
        }
        &.RUNNING {
            color: $runningColor;
        }
        &.SUCCESS {
            color: $successColor;
        }
        &.FAILED {
            color: $dangerColor;
        }
        &.SKIP {
            color: $pauseColor;
        }
    }
</style>
