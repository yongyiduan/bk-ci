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
                    RUNNING: `进度${this.getPercent()}（${this.job.matrixControlOption.finishCount}/${this.job.matrixControlOption.totalCount}）`
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
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: flex-end;
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
    .matrix-head-percent-main {
        font-size: 12px;
    }
</style>
